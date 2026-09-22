#if canImport(StoreKit)
import Foundation
import MongsModel
import StoreKit

/// 인앱 결제 (StoreKit 2)
///
/// Android `core/billing-core/.../GoogleBillingClient.kt` (239 LOC) 대응.
///
/// ## Google Play Billing → StoreKit 2 대응
///
/// | Android | 여기 |
/// |---|---|
/// | `queryProductDetails` | `Product.products(for:)` |
/// | `launchBillingFlow` + `PurchasesUpdatedListener` | `product.purchase()` — 결과가 그 자리에서 온다 |
/// | `queryPurchasesAsync` (미소비 주문 회수) | `Transaction.unfinished` |
/// | `consumeAsync` | `transaction.finish()` |
/// | `purchaseToken` | 서명된 트랜잭션 JWS (`jwsRepresentation`) |
/// | `orderId` | `transaction.id` |
///
/// **소모품(consumable)은 `finish()` 하기 전까지 `Transaction.unfinished` 에 남는다.**
/// 그게 Android 가 `queryPurchasesAsync` 로 하던 회수 경로와 정확히 같은 자리다.
/// 서버 지급이 끝난 뒤에만 `finish()` 해야 결제하고 못 받는 사고가 안 난다.
public actor PurchaseClient {

    public enum PurchaseOutcome: Sendable {
        case success(StoreOrder)
        /// 사용자가 취소했다. 오류가 아니다 — 배너를 띄우지 않는다.
        case cancelled
        /// 승인 대기 (가족 공유의 구매 요청 등). 나중에 `unfinishedOrders()` 로 들어온다.
        case pending
    }

    public init() {}

    /// 스토어에서 표시가를 가져온다. 못 찾은 상품은 결과에서 빠진다.
    ///
    /// App Store Connect 에 상품이 없으면 **빈 배열**이 온다 — 오류가 아니다.
    /// 로컬 검증은 Xcode 의 StoreKit Configuration 파일로 한다.
    public func displayPrices(for productIds: [String]) async -> [String: String] {
        guard !productIds.isEmpty else { return [:] }
        guard let products = try? await Product.products(for: productIds) else { return [:] }
        return Dictionary(uniqueKeysWithValues: products.map { ($0.id, $0.displayPrice) })
    }

    /// 결제를 시작한다.
    ///
    /// Android 는 `Activity` 를 넘겨 결제 화면을 띄우지만 StoreKit 은 시스템이 알아서 띄운다.
    public func purchase(productId: String) async throws -> PurchaseOutcome {
        guard let product = try await Product.products(for: [productId]).first else {
            throw StoreError.productNotFound(productId)
        }

        switch try await product.purchase() {
        case let .success(verification):
            return .success(try order(from: verification))
        case .userCancelled:
            return .cancelled
        case .pending:
            return .pending
        @unknown default:
            return .cancelled
        }
    }

    /// 아직 소비하지 않은 주문들.
    ///
    /// Android `getNotConsumedGoogleOrders()` 대응. 앱 진입과 복귀 때 회수한다 —
    /// 결제는 됐는데 서버 지급 전에 앱이 죽은 경우가 여기로 들어온다.
    public func unfinishedOrders() async -> [StoreOrder] {
        var orders: [StoreOrder] = []
        for await verification in Transaction.unfinished {
            guard let order = try? order(from: verification) else { continue }
            orders.append(order)
        }
        return orders
    }

    /// 서버 지급이 끝난 주문을 닫는다. **지급 전에 부르면 안 된다.**
    public func finish(socialOrderId: String) async {
        for await verification in Transaction.unfinished {
            guard case let .verified(transaction) = verification,
                  String(transaction.id) == socialOrderId else { continue }
            await transaction.finish()
            return
        }
    }

    /// 결제 화면 밖에서 도착하는 트랜잭션 (다른 기기, 승인 대기 해제 등).
    ///
    /// Android `PurchasesUpdatedListener` 자리다.
    public nonisolated func updates() -> AsyncStream<StoreOrder> {
        let (stream, continuation) = AsyncStream<StoreOrder>.makeStream()
        let task = Task {
            for await verification in Transaction.updates {
                guard case let .verified(transaction) = verification else { continue }
                continuation.yield(StoreOrder(
                    socialOrderId: String(transaction.id),
                    productId: transaction.productID,
                    purchaseToken: verification.jwsRepresentation
                ))
            }
            continuation.finish()
        }
        continuation.onTermination = { _ in task.cancel() }
        return stream
    }

    // MARK: - 내부

    /// 서명 검증을 통과한 것만 주문으로 만든다.
    ///
    /// StoreKit 2 는 검증을 **로컬에서** 해 준다 — Android 처럼 영수증을 서버로 보내
    /// 검증받을 필요가 없다. 다만 **지급의 근거로는 서버 검증이 여전히 필요하다**
    /// (기기 검증만 믿으면 탈옥 기기에서 위조할 수 있다). 그래서 JWS 를 서버로 넘긴다.
    private func order(from verification: VerificationResult<Transaction>) throws -> StoreOrder {
        guard case let .verified(transaction) = verification else {
            throw StoreError.unverified
        }
        return StoreOrder(
            socialOrderId: String(transaction.id),
            productId: transaction.productID,
            purchaseToken: verification.jwsRepresentation
        )
    }
}

public enum StoreError: Error, LocalizedError {
    case productNotFound(String)
    case unverified

    public var errorDescription: String? {
        switch self {
        case .productNotFound: "상품을 찾을 수 없습니다"
        case .unverified: "결제 검증에 실패했습니다"
        }
    }
}
#endif

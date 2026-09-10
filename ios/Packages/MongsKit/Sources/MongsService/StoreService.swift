import Foundation
import MongsModel

/// 인앱 상품 / 주문
///
/// Android `data/member-data/.../store/web/adapter/StoreWebAdapter.kt` +
/// `application/.../GetProductsUseCase.kt` 이식.
///
/// 서버와 스토어 양쪽을 봐야 화면을 그릴 수 있다:
/// 상품 목록은 서버가, 결제 여부는 스토어가, 지급 여부는 다시 서버가 안다.
public actor StoreService {

    private let api: APIClient
    private let purchases: PurchaseClient
    private let playerService: PlayerService

    public init(api: APIClient, purchases: PurchaseClient, playerService: PlayerService) {
        self.api = api
        self.purchases = purchases
        self.playerService = playerService
    }

    /// 화면에 그릴 상품 목록.
    ///
    /// Android `GetProductsUseCase` 와 같은 순서다:
    /// 1. 스토어에서 **미소비 주문**을 긁는다
    /// 2. 그중 **서버가 이미 지급한 것**을 빼낸다 (지급됐는데 `finish()` 를 못 한 주문)
    /// 3. 남은 주문을 상품에 붙인다 → 화면이 "소비" 버튼을 띄운다
    public func products() async throws -> [StoreProduct] {
        let pending = try await notConsumedOrders()

        let responses: [StoreResponse.Product] = try await api.request(
            Endpoint(host: .gateway, method: .get, path: "user/store/product")
        )

        // 표시가는 스토어에서 온다. 서버 가격은 스토어를 못 읽을 때의 대비책이다.
        let displayPrices = await purchases.displayPrices(for: responses.map(\.productId))

        return responses.map { response in
            StoreProduct(
                productId: response.productId,
                productName: response.productName,
                price: response.price,
                displayPrice: displayPrices[response.productId],
                orders: pending.filter {
                    $0.productId.caseInsensitiveCompare(response.productId) == .orderedSame
                }
            )
        }
    }

    /// 스토어에는 남아 있는데 서버는 아직 지급하지 않은 주문들.
    public func notConsumedOrders() async throws -> [StoreOrder] {
        let unfinished = await purchases.unfinishedOrders()
        guard !unfinished.isEmpty else { return [] }

        struct Request: Encodable { let socialOrderIds: [String] }
        let consumed: [StoreResponse.ConsumedOrder] = try await api.request(try Endpoint.json(
            host: .gateway,
            method: .post,
            path: "user/store/order",
            body: Request(socialOrderIds: unfinished.map(\.socialOrderId))
        ))

        let consumedIds = Set(consumed.map(\.socialOrderId))
        return unfinished.filter { !consumedIds.contains($0.socialOrderId) }
    }

    /// 주문을 서버에 넘겨 별가루를 받는다.
    ///
    /// **성공한 뒤에만 `finish()` 한다.** 먼저 닫으면 서버가 실패했을 때
    /// 스토어에도 주문이 안 남아 결제하고 못 받는 상태가 된다.
    @discardableResult
    public func consume(_ order: StoreOrder) async throws -> StoreResponse.Consume {
        struct Request: Encodable {
            let socialOrderId: String
            let productId: String
            let purchaseToken: String
        }

        let result: StoreResponse.Consume = try await api.request(try Endpoint.json(
            host: .gateway,
            method: .post,
            path: "user/store/order/consume",
            body: Request(
                socialOrderId: order.socialOrderId,
                productId: order.productId,
                purchaseToken: order.purchaseToken
            )
        ))

        await purchases.finish(socialOrderId: order.socialOrderId)

        // 응답이 갱신된 잔액을 주므로 따로 조회하지 않는다.
        await playerService.apply(slotCount: result.slotCount, starPoint: result.starPoint)
        return result
    }

    /// 결제를 시작한다. 결제만 하고 지급은 하지 않는다 — 호출 쪽이 `consume` 을 이어 부른다.
    public func purchase(productId: String) async throws -> PurchaseClient.PurchaseOutcome {
        try await purchases.purchase(productId: productId)
    }
}

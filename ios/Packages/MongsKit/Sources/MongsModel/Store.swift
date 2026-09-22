import Foundation

/// 인앱 상품
///
/// Android `domain/member-domain/.../store/model/Product.kt` + `ProductVo` 이식.
///
/// **가격은 서버가 주는 값이 아니라 스토어가 주는 값을 쓴다.** 서버 `price` 는 원화 정수지만,
/// 사용자가 실제로 결제하는 금액과 통화는 App Store 가 정한다(지역·세금·환율).
/// 서버 값은 상품을 식별·정렬하는 데만 쓰고 화면에는 스토어 표시가를 그린다.
public struct StoreProduct: Sendable, Equatable, Identifiable {

    public init(
        productId: String, productName: String, price: Double,
        displayPrice: String? = nil, orders: [StoreOrder] = []
    ) {
        self.productId = productId
        self.productName = productName
        self.price = price
        self.displayPrice = displayPrice
        self.orders = orders
    }

    public var id: String { productId }

    public let productId: String
    public let productName: String
    /// 서버가 주는 원화 가격. 스토어 표시가가 없을 때의 대비책이다.
    public let price: Double
    /// App Store 가 주는 지역화된 표시가 (`₩1,000`). 상품을 못 찾으면 nil 이다.
    public let displayPrice: String?
    /// 아직 소비하지 않은 이 상품의 주문들.
    ///
    /// 비어 있지 않으면 화면이 "구매" 대신 **"소비"** 버튼을 띄운다 —
    /// 결제는 됐는데 서버 지급이 안 끝난 주문을 사용자가 직접 회수할 수 있게 하는 폴백이다.
    public let orders: [StoreOrder]

    /// 화면에 그릴 가격. 스토어 표시가가 우선이다.
    public var priceText: String {
        if let displayPrice { return displayPrice }
        return "\(Int(price.rounded(.up)))원"
    }
}

/// 아직 소비하지 않은 주문
///
/// Android `Order` + `OrderVo` 이식.
public struct StoreOrder: Sendable, Equatable, Identifiable {

    public init(socialOrderId: String, productId: String, purchaseToken: String) {
        self.socialOrderId = socialOrderId
        self.productId = productId
        self.purchaseToken = purchaseToken
    }

    public var id: String { socialOrderId }

    /// 스토어가 매긴 주문 식별자.
    /// Android 는 Google 의 `orderId`, 여기서는 StoreKit `Transaction.id` 다.
    public let socialOrderId: String
    public let productId: String
    /// 서버가 스토어에 되물어 검증할 증표.
    ///
    /// Android 는 Google 의 `purchaseToken`,
    /// 여기서는 **서명된 트랜잭션 JWS**(`VerificationResult.jwsRepresentation`) 다.
    /// 서버가 App Store Server API 로 검증한다 — 백엔드 작업이 필요하다.
    public let purchaseToken: String
}

// MARK: - 서버 DTO

public enum StoreResponse {

    /// `GET user/store/product`
    public struct Product: Decodable, Sendable {
        public let productId: String
        public let productName: String
        public let price: Double
    }

    /// `POST user/store/order` — 이미 소비된 주문만 돌려준다.
    public struct ConsumedOrder: Decodable, Sendable {
        public let orderId: Int64
        public let socialOrderId: String
        public let productId: String
    }

    /// `POST user/store/order/consume`
    public struct Consume: Decodable, Sendable {
        public let accountId: Int64
        public let orderId: Int64
        public let socialOrderId: String
        public let productId: String
        public let starPoint: Int
        public let slotCount: Int
    }
}

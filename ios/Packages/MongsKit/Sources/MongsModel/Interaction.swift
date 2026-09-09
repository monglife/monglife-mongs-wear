import Foundation

/// 먹이 (밥/간식 공통)
///
/// Android `GetFoodResponseDto` / `GetSnackResponseDto` 이식.
/// 두 DTO 는 코드·이름 필드명만 다르고 나머지가 같아서 하나로 합쳤다.
public struct FeedItem: Sendable, Equatable, Identifiable {

    public init(
        kind: Kind, code: String, name: String, price: Int,
        weight: Double, strength: Double, satiety: Double,
        healthy: Double, fatigue: Double, canBuy: Bool
    ) {
        self.kind = kind
        self.code = code
        self.name = name
        self.price = price
        self.weight = weight
        self.strength = strength
        self.satiety = satiety
        self.healthy = healthy
        self.fatigue = fatigue
        self.canBuy = canBuy
    }

    /// `Identifiable` 은 SwiftUI `fullScreenCover(item:)` 이 요구한다.
    public enum Kind: String, Sendable, Equatable, Identifiable {
        case food
        case snack

        public var id: String { rawValue }

        /// 서버 경로 조각과 요청 필드 이름이 종류마다 다르다.
        public var path: String { self == .food ? "food" : "snack" }
        public var codeField: String { self == .food ? "foodCode" : "snackCode" }
    }

    public var id: String { code }

    public let kind: Kind
    public let code: String
    public let name: String
    public let price: Int
    /// 먹였을 때의 변화량
    public let weight: Double
    public let strength: Double
    public let satiety: Double
    public let healthy: Double
    public let fatigue: Double
    /// 살 수 있는지. 서버가 페이포인트를 보고 판단해 준다.
    public let canBuy: Bool
}

/// 인벤토리 항목
///
/// Android `domain/mong-domain/.../model/Inventory.kt` + `GetInventoryResponseDto` 이식.
public struct InventoryItem: Decodable, Sendable, Equatable, Identifiable {

    public var id: Int64 { inventoryId }

    public let inventoryId: Int64
    public let mongId: Int64
    public let inventoryCode: String
    public let inventoryName: String
    public let inventoryTypeCode: InventoryTypeCode
}

/// 인벤토리 항목 종류
///
/// Android `domain/mong-domain/.../enums/InventoryTypeCode.kt` 이식.
public enum InventoryTypeCode: String, Sendable, Codable, CaseIterable {
    case food = "FOOD"
    case snack = "SNACK"
    case map = "MAP"

    public var description: String {
        switch self {
        case .food: "먹이"
        case .snack: "간식"
        case .map: "맵"
        }
    }
}

// MARK: - 응답

extension MongResponse {

    /// 먹이 목록 (밥)
    public struct FoodList: Decodable, Sendable {
        public let foodCode: String
        public let foodName: String
        public let price: Int
        public let weight: Double
        public let strength: Double
        public let satiety: Double
        public let healthy: Double
        public let fatigue: Double
        public let isCanBuy: Bool
    }

    /// 먹이 목록 (간식)
    public struct SnackList: Decodable, Sendable {
        public let snackCode: String
        public let snackName: String
        public let price: Int
        public let weight: Double
        public let strength: Double
        public let satiety: Double
        public let healthy: Double
        public let fatigue: Double
        public let isCanBuy: Bool
    }

    /// 섭취 / 인벤토리 소비 결과
    ///
    /// Android `FeedFoodResponseDto` / `FeedSnackResponseDto` / `UseInventoryResponseDto` 가
    /// 필드가 완전히 같아서 하나로 합쳤다.
    public struct Consume: Decodable, Sendable {
        public let mongId: Int64
        public let payPoint: Int
        public let expRatio: Double
        public let strengthRatio: Double
        public let healthyRatio: Double
        public let satietyRatio: Double
        public let fatigueRatio: Double
        public let weight: Double
        public let stateCode: MongStateCode
        public let statusCode: MongStatusCode
    }
}

extension MongResponse.FoodList {
    public var item: FeedItem {
        FeedItem(kind: .food, code: foodCode, name: foodName, price: price,
                 weight: weight, strength: strength, satiety: satiety,
                 healthy: healthy, fatigue: fatigue, canBuy: isCanBuy)
    }
}

extension MongResponse.SnackList {
    public var item: FeedItem {
        FeedItem(kind: .snack, code: snackCode, name: snackName, price: price,
                 weight: weight, strength: strength, satiety: satiety,
                 healthy: healthy, fatigue: fatigue, canBuy: isCanBuy)
    }
}

extension Mong {

    /// Android `Mong.feed(...)` / `consumeInventory(...)`
    ///
    /// 두 응답이 같은 필드를 주므로 병합도 하나로 쓴다.
    /// 진화 응답과 달리 `mongCode` 와 `level` 은 바뀌지 않는다.
    public func applying(_ response: MongResponse.Consume) -> Mong {
        var next = self
        next.payPoint = response.payPoint
        next.expRatio = response.expRatio
        next.strengthRatio = response.strengthRatio
        next.healthyRatio = response.healthyRatio
        next.satietyRatio = response.satietyRatio
        next.fatigueRatio = response.fatigueRatio
        next.weight = response.weight
        next.stateCode = response.stateCode
        next.statusCode = response.statusCode
        return next
    }
}

/// 인벤토리 한 페이지
///
/// Android `GetInventoriesUseCase.Result` 대응. 화면이 페이지 인디케이터를 그리려면
/// 항목뿐 아니라 총 페이지 수도 필요하다.
public struct InventoryPage: Sendable, Equatable {

    public init(items: [InventoryItem], page: Int, totalPage: Int, isLastPage: Bool) {
        self.items = items
        self.page = page
        self.totalPage = totalPage
        self.isLastPage = isLastPage
    }

    public let items: [InventoryItem]
    /// 1-based. Android 와 같다.
    public let page: Int
    public let totalPage: Int
    public let isLastPage: Bool
}

import Foundation

/// 먹이 스프라이트 리소스
///
/// Android `assets/FoodResourceCode.kt` + `SnackResourceCode.kt` 이식.
/// 둘은 접두사만 다른 같은 모양이라 하나로 합치고 종류를 케이스 이름에 담았다.
/// 모르는 코드는 `mong_none` 으로 떨어진다 (Android 의 `FD444` / `SN444` 대응).
public enum FeedResourceCode: Sendable {

    private static let foodCodes: Set<String> = [
        "FD000", "FD010", "FD011", "FD012", "FD020", "FD021", "FD022", "FD030",
    ]

    private static let snackCodes: Set<String> = [
        "SN000", "SN001", "SN002", "SN010", "SN011", "SN012", "SN013",
    ]

    /// 번들 파일 이름 (확장자 없음)
    public static func pngName(kind: FeedItem.Kind, code: String) -> String {
        switch kind {
        case .food:
            foodCodes.contains(code) ? "food_\(code.lowercased())" : "mong_none"
        case .snack:
            snackCodes.contains(code) ? "snack_\(code.lowercased())" : "mong_none"
        }
    }

    /// 인벤토리는 종류 코드로 먹이/간식을 가른다. 맵은 아직 스프라이트가 없어 nil 이다.
    public static func pngName(for item: InventoryItem) -> String? {
        switch item.inventoryTypeCode {
        case .food: pngName(kind: .food, code: item.inventoryCode)
        case .snack: pngName(kind: .snack, code: item.inventoryCode)
        case .map: nil
        }
    }
}

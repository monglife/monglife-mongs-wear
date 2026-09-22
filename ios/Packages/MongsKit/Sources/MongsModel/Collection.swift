import Foundation

/// 도감 항목
///
/// Android `GetCollectionMapResponseDto` / `GetCollectionMongResponseDto` 이식.
/// 두 DTO 는 필드 이름(`mapCode`/`mongCode`)만 다르고 모양이 같아 하나로 합쳤다.
public struct CollectionItem: Sendable, Equatable, Identifiable {

    public enum Kind: String, Sendable, Equatable, Identifiable {
        case mong
        case map

        public var id: String { rawValue }
        /// 서버 경로 조각
        public var path: String { rawValue }
        public var title: String { self == .mong ? "몽 컬렉션" : "맵 컬렉션" }
    }

    public init(kind: Kind, code: String, name: String, isIncluded: Bool) {
        self.kind = kind
        self.code = code
        self.name = name
        self.isIncluded = isIncluded
    }

    public var id: String { code }

    public let kind: Kind
    public let code: String
    public let name: String
    /// 이미 모았는지. false 면 화면에 `?` 로 그린다.
    public let isIncluded: Bool
}

/// 맵 탐색 결과
///
/// Android `SearchCollectionMapResponseDto` 이식.
/// 못 찾으면 `isFound == false` 이고 `item` 이 nil 이다.
public struct MapSearchResult: Sendable, Equatable {

    public init(isFound: Bool, item: CollectionItem?) {
        self.isFound = isFound
        self.item = item
    }

    public let isFound: Bool
    public let item: CollectionItem?
}

// MARK: - 서버 DTO

public enum CollectionResponse {

    public struct Mong: Decodable, Sendable {
        public let mongCode: String
        public let mongName: String
        public let isIncluded: Bool

        public var item: CollectionItem {
            CollectionItem(kind: .mong, code: mongCode, name: mongName, isIncluded: isIncluded)
        }
    }

    public struct Map: Decodable, Sendable {
        public let mapCode: String
        public let mapName: String
        public let isIncluded: Bool

        public var item: CollectionItem {
            CollectionItem(kind: .map, code: mapCode, name: mapName, isIncluded: isIncluded)
        }
    }

    public struct Search: Decodable, Sendable {
        public let isFound: Bool
        public let data: Map?

        public var result: MapSearchResult {
            MapSearchResult(isFound: isFound, item: data?.item)
        }
    }
}

/// 맵 스프라이트
///
/// Android `assets/MapResourceCode.kt` 이식.
/// 모르는 코드는 기본 맵으로 떨어진다.
public enum MapResourceCode: Sendable {

    private static let codes: Set<String> = [
        "MP000", "MP001", "MP002", "MP003", "MP004", "MP005", "MP006", "MP007",
        "MP008", "MP009", "MP010", "MP011", "MP012", "MP013", "MP014", "MP015",
        "MP016", "MP017", "MP018", "MP019", "MP020", "MP023", "MP026", "MP027",
        "MP028", "MP029", "MP030", "MP031", "MP032", "MP037", "MP042",
    ]

    /// 번들 파일 이름 (확장자 없음)
    public static func pngName(_ code: String) -> String {
        codes.contains(code) ? "map_\(code.lowercased())" : "map_mp000"
    }
}

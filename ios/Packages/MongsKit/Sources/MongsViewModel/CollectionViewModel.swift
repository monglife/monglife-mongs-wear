import Foundation
import MongsModel
import MongsService
import Observation

/// 도감 ViewModel
///
/// Android `pages/collection/CollectionMongView` / `CollectionMapView` 의 ViewModel 이식.
/// 몽과 맵이 경로만 다르고 화면이 같아 하나로 합쳤다 (`FeedViewModel` 과 같은 이유).
@Observable
@MainActor
public final class CollectionViewModel: ErrorReportingViewModel {

    public private(set) var isLoading = true
    public private(set) var items: [CollectionItem] = []
    /// 탭한 항목의 이름을 띄운다. 못 모은 항목은 nil 이다.
    public var selected: CollectionItem?

    public let kind: CollectionItem.Kind

    private let service: CollectionService

    public init(kind: CollectionItem.Kind, service: CollectionService) {
        self.kind = kind
        self.service = service
    }

    public func load() async {
        isLoading = true
        defer { isLoading = false }
        await run { self.items = try await self.service.items(kind: self.kind) }
    }

    /// 못 모은 항목은 눌러도 아무 일도 하지 않는다 — 원본도 `?` 버튼에 동작이 없다.
    public func select(_ item: CollectionItem) {
        guard item.isIncluded else { return }
        selected = item
    }

    public func clearSelection() { selected = nil }

    public func recoverFromError(_ error: any Error) async {
        isLoading = false
    }
}

/// 맵 탐색 ViewModel
///
/// Android `pages/map/SearchMapViewModel` 이식.
@Observable
@MainActor
public final class MapSearchViewModel: ErrorReportingViewModel {

    public enum Phase: Sendable, Equatable {
        case idle
        /// GPS 를 읽고 서버에 묻는 중
        case searching
        case found(CollectionItem)
        case notFound
        /// 위치 권한이 거부돼 있다. 설정 앱에서만 바꿀 수 있다.
        case denied
    }

    public private(set) var phase: Phase = .idle

    private let service: CollectionService
    private let location: LocationClient

    public init(service: CollectionService, location: LocationClient) {
        self.service = service
        self.location = location
    }

    public func search() async {
        guard phase != .searching else { return }
        phase = .searching

        do {
            let result = try await service.searchMap()
            if result.isFound, let item = result.item {
                phase = .found(item)
            } else {
                phase = .notFound
            }
        } catch LocationClient.LocationError.denied {
            // 배너 대신 화면에 남긴다 — 사용자가 설정 앱으로 가야 하는 상황이다.
            phase = .denied
        } catch {
            await ErrorBanner.shared.post(error: error)
            phase = .idle
        }
    }

    public func reset() { phase = .idle }

    public func recoverFromError(_ error: any Error) async {
        phase = .idle
    }
}

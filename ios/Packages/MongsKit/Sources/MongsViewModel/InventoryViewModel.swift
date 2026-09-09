import Foundation
import MongsModel
import MongsService
import Observation

/// 인벤토리 ViewModel
///
/// Android `pages/inventory/InventoryViewModel.kt` 이식.
///
/// 한 페이지에 4칸(2×2)씩 **서버 페이징**으로 보여준다. 클라이언트에서 자르지 않는다 —
/// 원본이 `page`/`size=4` 를 서버에 그대로 넘기고 `totalPage` 로 인디케이터를 그린다.
@Observable
@MainActor
public final class InventoryViewModel: ErrorReportingViewModel {

    /// Android `INIT_PAGE` / `INIT_SIZE`
    private static let initialPage = 1
    private static let pageSize = 4

    public private(set) var isLoading = true
    public private(set) var items: [InventoryItem] = []
    /// 1-based
    public private(set) var page = InventoryViewModel.initialPage
    public private(set) var totalPage = 0
    /// 사용 확인 다이얼로그 대상
    public private(set) var pending: InventoryItem?
    /// 사용이 끝나 화면을 닫아야 하는지
    public private(set) var isFinished = false

    public var isFirstPage: Bool { page <= Self.initialPage }
    public var isLastPage: Bool { page >= totalPage }

    /// 2×2 격자를 그리려면 빈 칸도 자리를 차지해야 한다.
    /// Android 는 `inventoryVos[0..3]` 을 하나씩 꺼내며 없으면 null 을 넘긴다.
    public var slots: [InventoryItem?] {
        (0..<Self.pageSize).map { items.indices.contains($0) ? items[$0] : nil }
    }

    private let mongService: MongService

    public init(mongService: MongService) {
        self.mongService = mongService
    }

    public func load() async {
        isLoading = true
        defer { isLoading = false }

        await run {
            let result = try await self.mongService.inventory(page: self.page, size: Self.pageSize)
            self.items = result.items
            self.totalPage = result.totalPage
        }
    }

    public func previousPage() async {
        guard !isFirstPage else { return }
        page = max(page - 1, Self.initialPage)
        await load()
    }

    public func nextPage() async {
        guard !isLastPage else { return }
        page = min(page + 1, totalPage)
        await load()
    }

    public func askConfirm(_ item: InventoryItem) { pending = item }
    public func cancelConfirm() { pending = nil }

    public func use() async {
        guard let item = pending else { return }
        pending = nil
        isLoading = true
        defer { isLoading = false }

        await run {
            try await self.mongService.useInventory(item)
            self.isFinished = true
        }
    }

    public func recoverFromError(_ error: any Error) async {
        pending = nil
        isLoading = false
    }
}

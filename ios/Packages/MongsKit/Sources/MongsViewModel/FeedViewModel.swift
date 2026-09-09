import Foundation
import MongsModel
import MongsService
import Observation

/// 먹이주기 (밥/간식) ViewModel
///
/// Android `pages/feed/FeedFoodViewModel.kt` + `FeedSnackViewModel.kt` 이식.
/// 원본은 둘을 따로 뒀지만 서버 경로와 필드명만 다르고 화면이 완전히 같아서 하나로 합쳤다.
///
/// **목록이 아니라 캐러셀이다.** 한 번에 한 종류만 보이고 좌우 버튼으로 넘긴다.
@Observable
@MainActor
public final class FeedViewModel: ErrorReportingViewModel {

    public private(set) var isLoading = true
    public private(set) var items: [FeedItem] = []
    public private(set) var index = 0
    public private(set) var payPoint = 0
    /// 상세 다이얼로그(스탯 변화량) 표시 여부
    public var isDetailPresented = false
    /// 구매 확인 다이얼로그 표시 여부
    public var isConfirmPresented = false
    /// 먹이기가 끝나 화면을 닫아야 하는지
    public private(set) var isFinished = false

    public let kind: FeedItem.Kind

    public var current: FeedItem? {
        items.indices.contains(index) ? items[index] : nil
    }

    /// Android `BlueButton(disable = price > payPoint || !isCanBuy)`
    public var canBuyCurrent: Bool {
        guard let item = current else { return false }
        return item.canBuy && item.price <= payPoint
    }

    public var isFirst: Bool { index == 0 }
    public var isLast: Bool { index >= items.count - 1 }

    private let mongService: MongService

    public init(kind: FeedItem.Kind, mongService: MongService) {
        self.kind = kind
        self.mongService = mongService
    }

    public func load() async {
        isLoading = true
        defer { isLoading = false }

        payPoint = await mongService.currentMong()?.payPoint ?? 0

        await run {
            self.items = try await self.mongService.feedItems(kind: self.kind)
            self.index = min(self.index, max(self.items.count - 1, 0))
        }
    }

    public func previous() { index = max(index - 1, 0) }
    public func next() { index = min(index + 1, max(items.count - 1, 0)) }

    public func openDetail() { isDetailPresented = true }
    public func closeDetail() { isDetailPresented = false }

    public func openConfirm() {
        guard canBuyCurrent else { return }
        isConfirmPresented = true
    }
    public func closeConfirm() { isConfirmPresented = false }

    /// Android `FeedFoodViewModel.buy()` — 성공하면 `UiEvent.Buy` 로 메인까지 되돌아간다.
    public func buy() async {
        guard let item = current else { return }
        isConfirmPresented = false
        isLoading = true
        defer { isLoading = false }

        await run {
            try await self.mongService.feed(item)
            self.isFinished = true
        }
    }

    public func recoverFromError(_ error: any Error) async {
        isDetailPresented = false
        isConfirmPresented = false
        isLoading = false
    }
}

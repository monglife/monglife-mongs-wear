import Foundation
import MongsModel
import MongsService
import Observation

/// 공지사항 ViewModel
///
/// Android `pages/notice/NoticeViewModel.kt` 이식.
/// 리스트 끝에 닿으면 다음 쪽을 이어 붙이는 **무한 스크롤**이다 (원본 `OnScroll`).
@Observable
@MainActor
public final class NoticeViewModel: ErrorReportingViewModel {

    public private(set) var isLoading = true
    /// 다음 쪽을 불러오는 중. 리스트 맨 아래 작은 로딩바로 그린다.
    public private(set) var isLoadingMore = false
    public private(set) var notices: [Notice] = []
    /// 상세 다이얼로그에 띄울 본문. nil 이면 닫힘.
    public var detail: String?

    private var page = 1
    private var isLastPage = false

    private let service: CommunityService

    public init(service: CommunityService) {
        self.service = service
    }

    public func load() async {
        isLoading = true
        defer { isLoading = false }

        page = 1
        isLastPage = false
        await run {
            let first = try await self.service.notices(page: 1)
            self.notices = first
            self.isLastPage = first.isEmpty
        }
    }

    /// 리스트 끝에 닿았을 때. 마지막 쪽이면 아무것도 하지 않는다.
    public func loadMoreIfNeeded() async {
        guard !isLastPage, !isLoadingMore, !isLoading else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }

        await run {
            let next = try await self.service.notices(page: self.page + 1)
            if next.isEmpty {
                self.isLastPage = true
                return
            }
            self.page += 1
            // 서버가 같은 쪽을 다시 줘도 목록이 부풀지 않게 거른다.
            let existing = Set(self.notices.map(\.noticeId))
            self.notices += next.filter { !existing.contains($0.noticeId) }
        }
    }

    public func openDetail(_ notice: Notice) { detail = notice.content }
    public func closeDetail() { detail = nil }

    public func recoverFromError(_ error: any Error) async {
        isLoading = false
        isLoadingMore = false
    }
}

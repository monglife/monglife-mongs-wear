import Foundation
import MongsModel
import MongsService
import Observation

/// 오류 신고 ViewModel
///
/// Android `pages/feedback/FeedbackViewModel.kt` 이식.
@Observable
@MainActor
public final class FeedbackViewModel: ErrorReportingViewModel {

    /// 원본 `CreateFeedbackDialog` 의 두 단계 — 제목을 받고 나서 내용을 받는다.
    public enum Step: Sendable, Equatable {
        case title
        case content
    }

    public private(set) var isLoading = false
    public var step: Step = .title
    public var title = ""
    public var content = ""
    /// 등록이 끝나 화면을 닫아야 하는지
    public private(set) var didSubmit = false

    public var canGoNext: Bool { !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
    public var canSubmit: Bool {
        canGoNext && !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    private let service: CommunityService

    public init(service: CommunityService) {
        self.service = service
    }

    public func next() { if canGoNext { step = .content } }
    public func back() { step = .title }

    public func submit() async {
        guard canSubmit, !isLoading else { return }
        isLoading = true
        defer { isLoading = false }

        await run {
            try await self.service.submitFeedback(title: self.title, content: self.content)
            self.didSubmit = true
        }
    }

    public func recoverFromError(_ error: any Error) async {
        isLoading = false
    }
}

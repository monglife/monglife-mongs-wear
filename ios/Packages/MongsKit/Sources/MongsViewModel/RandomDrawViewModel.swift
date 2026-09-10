import Foundation
import MongsModel
import MongsService
import Observation

/// 랜덤 뽑기 ViewModel
///
/// Android `pages/randomDraw/RandomDrawViewModel.kt` 이식.
@Observable
@MainActor
public final class RandomDrawViewModel: ErrorReportingViewModel {

    /// 뽑기 한 번의 가격. Android `DRAW_PAY_POINT`.
    public static let drawPayPoint = 100
    /// 뽑기 기계가 흔들리는 시간. Android `DRAW_DELAY`.
    private static let drawDelay: Duration = .seconds(2)

    /// 원본 `UiState` 를 그대로 옮긴 화면 단계.
    public enum Phase: Sendable, Equatable {
        case loading
        /// 뽑기 안내 다이얼로그
        case entering
        /// 확인 다이얼로그
        case confirm
        /// 뽑는 중 — 기계가 흔들린다
        case drawing
        /// 결과 다이얼로그
        case result
    }

    public private(set) var phase: Phase = .loading
    public private(set) var mong: Mong?
    public private(set) var result: RandomDrawResult?
    /// 몽이 없어 화면을 닫아야 하는지
    public private(set) var shouldClose = false

    public var payPoint: Int { mong?.payPoint ?? 0 }
    public var ticketCount: Int { mong?.randomDrawTicketCount ?? 0 }

    /// 원본 `disable = randomDrawPayPoint > payPoint && randomDrawTicketCount <= 0`.
    /// **둘 중 하나만 있으면 된다** — 티켓이 있거나, 페이포인트가 충분하거나.
    public var canDraw: Bool {
        ticketCount > 0 || payPoint >= Self.drawPayPoint
    }

    private let mongService: MongService

    public init(mongService: MongService) {
        self.mongService = mongService
    }

    public func load() async {
        phase = .loading
        mong = await mongService.currentMong()
        guard mong != nil else {
            shouldClose = true
            return
        }
        phase = .entering
    }

    public func askConfirm() {
        guard canDraw else { return }
        phase = .confirm
    }

    public func cancelConfirm() { phase = .entering }

    /// 뽑기. 티켓과 페이포인트 중 무엇을 쓸지는 **서버가 정한다** —
    /// 클라이언트는 그냥 요청만 보낸다 (원본도 같다).
    public func draw() async {
        guard phase == .confirm else { return }
        phase = .drawing

        // 기계가 흔들리는 연출이 보일 시간을 준다. 원본도 요청 전에 기다린다.
        try? await Task.sleep(for: Self.drawDelay)

        await run {
            self.result = try await self.mongService.randomDraw()
            self.mong = await self.mongService.currentMong()
            self.phase = .result
        }
    }

    public func closeResult() {
        result = nil
        phase = .entering
    }

    public func recoverFromError(_ error: any Error) async {
        phase = .entering
    }
}

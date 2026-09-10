import Foundation
import MongsModel
import MongsService
import Observation

/// 훈련 ViewModel
///
/// Android `pages/training/*ViewModel.kt` 5개(1,186 LOC) 이식.
///
/// 원본은 미니게임마다 ViewModel 을 따로 뒀지만 **껍데기가 거의 같다** —
/// 참가 확인 → 플레이 → 점수 보고 → 결과. 다른 건 가운데 "플레이" 부분뿐이라
/// 공통 흐름을 여기 하나로 모으고, 게임별 상태는 각 엔진이 들고 있게 했다.
@Observable
@MainActor
public final class TrainingViewModel: ErrorReportingViewModel {

    /// 원본 `UiState` 대응.
    public enum Phase: Sendable, Equatable {
        case loading
        /// 참가 안내 다이얼로그
        case entering
        case playing
        /// 결과 다이얼로그
        case over(TrainingResult)
    }

    public private(set) var phase: Phase = .loading
    public private(set) var type: TrainingType?
    public private(set) var mong: Mong?
    public private(set) var score = 0
    /// 남은 시간(초). `timeout == 0` 인 종목(달리기)은 nil 이다.
    public private(set) var remainingSeconds: Int?
    public private(set) var shouldClose = false

    /// 참가비를 낼 수 있는지. 원본도 페이포인트만 본다.
    public var canEnter: Bool {
        guard let type, let mong else { return false }
        return mong.payPoint >= type.payPoint
    }

    private let code: String
    private let service: TrainingService
    private let mongService: MongService
    private var timerTask: Task<Void, Never>?

    public init(code: String, service: TrainingService, mongService: MongService) {
        self.code = code
        self.service = service
        self.mongService = mongService
    }

    public func load() async {
        phase = .loading
        mong = await mongService.currentMong()
        guard mong != nil else {
            shouldClose = true
            return
        }

        await run {
            let types = try await self.service.types()
            guard let type = types.first(where: { $0.trainingCode == self.code }) else {
                self.shouldClose = true
                return
            }
            self.type = type
            self.phase = .entering
        }
    }

    /// 플레이 시작. 제한 시간이 있는 종목은 여기서 타이머가 돈다.
    public func begin() {
        guard canEnter, let type else { return }
        score = 0
        phase = .playing

        guard type.timeout > 0 else {
            remainingSeconds = nil
            return
        }

        remainingSeconds = type.timeout
        timerTask?.cancel()
        timerTask = Task { [weak self] in
            while !Task.isCancelled {
                try? await Task.sleep(for: .seconds(1))
                guard let self, let left = self.remainingSeconds else { return }
                if left <= 1 {
                    self.remainingSeconds = 0
                    await self.finish()
                    return
                }
                self.remainingSeconds = left - 1
            }
        }
    }

    public func addScore(_ amount: Int = 1) {
        guard phase == .playing else { return }
        score += amount
    }

    /// 게임이 끝났다 (시간 초과이거나 죽었거나).
    ///
    /// **점수만 보내고 성패는 서버가 정한다** — 원본도 같다.
    public func finish() async {
        guard phase == .playing else { return }
        timerTask?.cancel()
        timerTask = nil

        let reported = score
        await run {
            let result = try await self.service.end(code: self.code, score: reported)
            self.mong = await self.mongService.currentMong()
            self.phase = .over(result)
        }
    }

    public func closeResult() { shouldClose = true }

    public func cancelTimer() {
        timerTask?.cancel()
        timerTask = nil
    }

    public func recoverFromError(_ error: any Error) async {
        cancelTimer()
        phase = .entering
    }
}

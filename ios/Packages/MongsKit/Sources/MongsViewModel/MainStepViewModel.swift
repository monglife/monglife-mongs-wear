import MongsModel
import MongsService
import Observation

/// 메인 화면 걸음 수 ViewModel
///
/// Android `presentation/viewmodel-presentation/.../pages/main/MainStepViewModel.kt` 이식.
///
/// 원본은 Hilt 로 UseCase 3개를 주입받고 `StateFlow` 로 상태를 노출한다.
/// MVVM 으로 접으면서 UseCase 레이어를 없앴으므로 의존성은 `StepService` 하나다.
/// SwiftUI 뷰는 프로퍼티를 읽기만 하면 자동으로 갱신되므로
/// `StateFlow` → `collectAsState()` 짝에 해당하는 코드가 필요 없다.
@Observable
@MainActor
public final class MainStepViewModel {

    /// UI 상태 정의 — Android 의 `sealed class UiState(loadingBar: Boolean)` 대응
    public enum UiState: Equatable, Sendable {
        case idle
        case loading

        public var loadingBar: Bool { self == .loading }
    }

    public private(set) var uiState: UiState = .idle

    /// available = false 로 시작한다.
    /// 수집 경로가 정해지기 전에는 0 이 아니라 "-" 를 보여야 한다.
    public private(set) var step = Step(walkingCount: 0, available: false)

    /// 마지막 환전으로 받은 payPoint
    public private(set) var lastExchangedPayPoint: Int?

    /// Android `BaseViewModel.errorEvent` (전역 토스트 채널) 대응.
    /// 화면이 늘어나면 전역 배너 스트림으로 옮긴다.
    public private(set) var errorMessage: String?

    private let stepService: any StepService

    public init(stepService: any StepService) {
        self.stepService = stepService
    }

    /// 걸음 수 수집을 시작하고 스트림을 화면에 물린다.
    ///
    /// Android 는 `init { viewModelScopeWithHandler.launch { ... } }` 로 생성 시점에 돌면서
    /// `observeForever` 로 StateFlow 에 꽂는다. SwiftUI 에서는 이 메서드를 `.task {}` 에
    /// 걸어 두면 뷰가 사라질 때 구독이 함께 취소되므로 Task 를 따로 붙들 필요가 없다.
    /// (Task 를 저장해 두면 deinit 에서 취소해야 하는데, `@MainActor` 격리 때문에
    ///  Swift 6 에서는 nonisolated deinit 이 그 프로퍼티를 건드릴 수 없다.)
    public func observeWalkingCount() async {
        uiState = .loading

        await stepService.startCollection()
        let stream = await stepService.stepStream()
        uiState = .idle

        for await next in stream {
            step = next
        }
    }

    /// 환전 가능한 최대 단위를 한 번에 환전한다.
    public func exchange() async {
        let units = step.exchangeableUnits
        guard units > 0 else { return }

        uiState = .loading
        defer { uiState = .idle }

        do {
            let result = try await stepService.exchange(units: units)
            lastExchangedPayPoint = result.payPoint
            errorMessage = nil
        } catch {
            handle(error)
        }
    }

    public func clearError() {
        errorMessage = nil
    }

    /// Android `BaseViewModel` 의 `CoroutineExceptionHandler` 대응 —
    /// `ErrorCode.isMessageShow()` 가 true 인 것만 사용자에게 보여준다.
    private func handle(_ error: any Error) {
        if let deviceError = error as? DeviceError, deviceError.isMessageShown {
            errorMessage = deviceError.message
        } else {
            errorMessage = "알 수 없는 오류가 발생했습니다."
        }
    }
}

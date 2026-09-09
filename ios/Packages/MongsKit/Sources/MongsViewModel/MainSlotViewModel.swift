import Foundation
import MongsModel
import MongsService
import Observation

/// 메인 슬롯(펫) ViewModel
///
/// Android `presentation/viewmodel-presentation/.../pages/main/MainSlotViewModel.kt` (278 LOC) 이식.
/// 원본은 UI 상태 9개를 두고 상호작용마다 상태를 갈아끼운다.
@Observable
@MainActor
public final class MainSlotViewModel: ErrorReportingViewModel {

    /// UI 상태
    ///
    /// Android 의 `sealed class UiState` 9종을 진행 중 여부 하나로 접었다.
    /// 원본이 상태를 잘게 나눈 건 상호작용별로 다른 이펙트(똥 치우기 애니메이션 등)를
    /// 띄우기 위해서인데, 그 이펙트들은 아직 이식 전이다.
    public enum UiState: Equatable, Sendable {
        case idle
        case loading
        /// 상호작용 진행 중. 버튼을 잠가 중복 요청을 막는다.
        case interacting

        public var loadingBar: Bool { self == .loading }
        public var isBusy: Bool { self != .idle }
    }

    public private(set) var uiState: UiState = .idle
    public private(set) var mong: Mong?

    /// 쓰다듬기 직후 잠깐 기뻐하는 표정을 띄운다.
    public private(set) var isHappy = false

    /// 진화 연출이 도는 중인지.
    ///
    /// 원본은 이 값이 true 인 동안 몽을 **정적 PNG 로** 그리고 그 위에 이펙트 3장을
    /// 순서대로 덮는다. 연출이 끝나면 그때 서버에 진화를 요청한다 —
    /// 순서가 반대면 몽이 먼저 바뀌어 버려 연출이 무의미해진다.
    public private(set) var isEvolving = false

    /// 똥 치우기 연출이 도는 중인지.
    public private(set) var isPoopCleaning = false

    /// 상호작용 다이얼로그가 열려 있는지.
    ///
    /// Android 는 버튼을 화면에 두지 않는다 — 몽을 탭하면 전체를 덮는 다이얼로그가 열리고
    /// 거기에 재우기 / 쓰다듬기 / 똥치우기 / 먹이주기 / 인벤토리가 들어 있다.
    public var isInteractionDialogOpen = false

    /// 최초 조회가 끝났는지.
    ///
    /// 페이저가 이걸 기다린다 — 몽 유무에 따라 **쪽 수가 달라지기** 때문에,
    /// 정해지기 전에 페이저를 만들면 쪽 수가 도중에 바뀌면서 선택된 쪽이 어긋난다.
    /// Android 도 `uiState.loadingBar` 를 먼저 보고 그 다음에 페이저를 만든다.
    public private(set) var hasLoaded = false

    private let mongService: MongService

    public init(mongService: MongService) {
        self.mongService = mongService
    }

    /// 캐시 스트림을 화면에 물리고 서버에서 최신값을 받아온다.
    ///
    /// 캐시를 먼저 그리는 순서가 중요하다 — 네트워크를 기다리는 동안 빈 화면을 보여주지 않는다.
    public func observe() async {
        let stream = await mongService.currentMongStream()

        uiState = .loading
        await run { try await self.mongService.refresh() }

        // ⚠️ hasLoaded 를 올리기 전에 현재 값을 반영한다.
        // 아래 for-await 루프는 아직 한 번도 돌지 않았으므로, 여기서 채우지 않으면
        // "로딩은 끝났는데 몽은 nil" 인 순간이 생긴다.
        // 페이저가 그 순간의 몽 유무로 쪽 수를 정하기 때문에 한 프레임 차이가 그대로 버그가 된다.
        mong = await mongService.currentMong()

        uiState = .idle
        hasLoaded = true

        for await next in stream {
            mong = next
        }
    }

    // MARK: - 상호작용

    /// 다이얼로그를 연다. 상호작용할 수 없는 상태면 열지 않는다.
    public func openInteractionDialog() {
        guard mong?.isInteractable == true else { return }
        isInteractionDialogOpen = true
    }

    public func closeInteractionDialog() {
        isInteractionDialogOpen = false
    }

    public func stroke() async {
        await interact {
            try await self.mongService.stroke()
            // 원본은 쓰다듬기 후 하트 이펙트를 띄운다. 표정만 우선 옮겼다.
            self.isHappy = true
            try? await Task.sleep(for: .seconds(2))
            self.isHappy = false
        }
    }

    public func toggleSleep() async {
        await interact { try await self.mongService.toggleSleep() }
    }

    public func cleanPoop() async {
        isPoopCleaning = true
        defer { isPoopCleaning = false }

        await interact { try await self.mongService.cleanPoop() }
        // 청소 연출(진공청소기 GIF)이 보일 시간을 준다. 원본도 이펙트가 한 바퀴 돈다.
        try? await Task.sleep(for: .seconds(1.2))
    }

    /// 진화 연출을 시작한다. 실제 진화 요청은 연출이 끝난 뒤다.
    public func startEvolution() {
        guard mong?.canEvolve == true, !uiState.isBusy else { return }
        isInteractionDialogOpen = false
        isEvolving = true
    }

    /// 진화 연출이 끝났을 때 호출된다.
    public func evolve() async {
        defer { isEvolving = false }
        await interact { try await self.mongService.evolve() }
    }

    public func graduate() async {
        await interact { try await self.mongService.graduate() }
    }

    /// 졸업 연출이 끝났을 때. 원본 `graduateMongCheck` 자리다 —
    /// 연출을 한 번 본 뒤에는 다시 보여주지 않는다.
    public private(set) var didPlayGraduation = false

    public func markGraduationPlayed() {
        didPlayGraduation = true
    }

    /// 상호작용 공통 처리
    ///
    /// **진행 중에는 다시 못 누르게 막는다.** Android 가 결제에서 같은 버그를 겪고
    /// 오버레이로 터치를 막았다(커밋 `7185905`). 여기서는 상태로 막는다.
    private func interact(_ body: @escaping () async throws -> Void) async {
        guard !uiState.isBusy, mong?.isInteractable == true else { return }

        uiState = .interacting
        isInteractionDialogOpen = false
        defer { uiState = .idle }

        await run { try await body() }
    }

    public func recoverFromError(_ error: any Error) async {
        uiState = .idle
    }
}

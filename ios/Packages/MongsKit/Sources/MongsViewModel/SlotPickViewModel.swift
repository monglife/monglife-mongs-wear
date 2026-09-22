import Foundation
import MongsModel
import MongsService
import Observation

/// 슬롯 관리 ViewModel
///
/// Android `presentation/viewmodel-presentation/.../pages/slotPick/SlotPickViewModel.kt` 이식.
///
/// 원본은 다이얼로그 종류마다 UiState 를 하나씩 뒀지만(8종), 여기서는
/// **어떤 다이얼로그가 열려 있는가**를 값 하나로 표현한다 — 동시에 두 개가 열릴 수 없다는
/// 사실이 타입에 드러난다.
@Observable
@MainActor
public final class SlotPickViewModel: ErrorReportingViewModel {

    public enum Dialog: Equatable, Sendable {
        case create
        case detail
        case confirmDelete
        case confirmPick
        case confirmGraduate
        case confirmBuySlot
    }

    public private(set) var isLoading = false
    public private(set) var slots: [Slot] = []
    public private(set) var index = 0
    public private(set) var starPoint = 0
    /// 지금 메인 화면이 보고 있는 몽. "선택" 버튼을 잠그는 데 쓴다.
    public private(set) var currentMongId: Int64?

    public var dialog: Dialog?

    /// 선택을 마치고 메인으로 돌아가야 할 때 true 가 된다.
    public private(set) var shouldReturnToMain = false

    private let mongService: MongService
    private let playerService: PlayerService

    public init(mongService: MongService, playerService: PlayerService) {
        self.mongService = mongService
        self.playerService = playerService
    }

    public var currentSlot: Slot? {
        slots.indices.contains(index) ? slots[index] : nil
    }

    public var canGoPrevious: Bool { index > 0 }
    public var canGoNext: Bool { index < slots.count - 1 }

    /// 슬롯을 살 수 있는 별가루가 있는지
    public var canAffordSlot: Bool { starPoint >= Slot.purchasePrice }

    // MARK: - 로딩

    public func load() async {
        isLoading = true
        defer { isLoading = false }

        currentMongId = await mongService.currentMong()?.mongId

        await run {
            // 플레이어가 없으면 만들고 다시 읽는다. 계정 첫 진입 경로다.
            let player: Player
            do {
                player = try await self.playerService.refresh()
            } catch {
                try await self.playerService.create()
                player = try await self.playerService.refresh()
            }
            self.starPoint = player.starPoint

            let mongs = try await self.mongService.allMongs()
            self.slots = Slot.build(mongs: mongs, slotCount: player.slotCount)
            self.clampIndex()
        }
    }

    // MARK: - 이동

    public func goPrevious() {
        index = max(index - 1, 0)
    }

    public func goNext() {
        index = min(index + 1, slots.count - 1)
    }

    // MARK: - 동작

    public func createMong(name: String, sleepAt: Date, wakeupAt: Date) async {
        await perform {
            try await self.mongService.create(name: name, sleepAt: sleepAt, wakeupAt: wakeupAt)
        }
    }

    public func deleteMong(_ mong: Mong) async {
        await perform { try await self.mongService.delete(mongId: mong.mongId) }
    }

    public func graduateMong(_ mong: Mong) async {
        await perform { try await self.mongService.graduate() }
    }

    public func buySlot() async {
        await perform {
            let player = try await self.playerService.buySlot()
            self.starPoint = player.starPoint
        }
    }

    /// 몽을 선택하고 메인으로 돌아간다.
    public func pickMong(_ mong: Mong) async {
        dialog = nil
        isLoading = true
        defer { isLoading = false }

        await mongService.select(mong)
        currentMongId = mong.mongId
        shouldReturnToMain = true
    }

    public func didReturnToMain() {
        shouldReturnToMain = false
    }

    // MARK: - 내부

    /// 다이얼로그를 닫고 동작을 수행한 뒤 목록을 다시 읽는다.
    ///
    /// 생성·삭제·졸업·구매는 전부 슬롯 구성을 바꾸므로 목록을 새로 받아야 한다.
    private func perform(_ body: @escaping () async throws -> Void) async {
        dialog = nil
        isLoading = true
        defer { isLoading = false }

        await run {
            try await body()

            let player = try await self.playerService.refresh()
            self.starPoint = player.starPoint

            let mongs = try await self.mongService.allMongs()
            self.slots = Slot.build(mongs: mongs, slotCount: player.slotCount)
            self.clampIndex()
        }
    }

    /// 슬롯이 줄면(삭제·졸업) 인덱스가 범위를 벗어난다.
    private func clampIndex() {
        index = min(max(index, 0), max(slots.count - 1, 0))
    }

    public func recoverFromError(_ error: any Error) async {
        dialog = nil
        isLoading = false
    }
}

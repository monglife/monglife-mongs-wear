import Foundation
import MongsModel
import MongsService
import Observation

/// 환전 ViewModel
///
/// Android `pages/exchange/ExchangeStepViewModel.kt` + `ExchangeStarPointViewModel.kt` 이식.
/// 두 화면이 "단위를 골라 확인 후 환전" 이라는 같은 모양이라 하나로 합쳤다.
@Observable
@MainActor
public final class ExchangeViewModel: ErrorReportingViewModel {

    /// 무엇을 환전하는가
    ///
    /// `Identifiable` 인 이유는 SwiftUI `fullScreenCover(item:)` 이 요구해서다 —
    /// 이 값 하나로 "어떤 환전 화면이 열려 있는가" 를 표현한다.
    public enum Kind: String, Sendable, Equatable, Identifiable {
        /// 걸음 → 페이포인트. 1000걸음 = 100P
        case step
        /// 별가루 → 페이포인트. 1별가루 = 1P
        case starPoint

        public var id: String { rawValue }

        public var title: String {
            switch self {
            case .step: "걸음 환전"
            case .starPoint: "별가루 환전"
            }
        }
    }

    public let kind: Kind

    public private(set) var isLoading = false
    public private(set) var isConfirming = false
    /// 환전에 쓸 수 있는 잔액 (걸음 수 또는 별가루)
    public private(set) var available = 0
    /// 지금 고른 단위 수
    public private(set) var units = 0
    public private(set) var mong: Mong?
    /// 환전이 끝나면 true. 화면을 닫는 신호다.
    public private(set) var didFinish = false

    private let stepService: any StepService
    private let playerService: PlayerService
    private let mongService: MongService

    public init(
        kind: Kind,
        stepService: any StepService,
        playerService: PlayerService,
        mongService: MongService
    ) {
        self.kind = kind
        self.stepService = stepService
        self.playerService = playerService
        self.mongService = mongService
    }

    /// 고를 수 있는 최대 단위 수
    public var maxUnits: Int {
        switch kind {
        case .step: StepExchangeRate.maxExchangeableUnits(walkingCount: available)
        case .starPoint: available
        }
    }

    /// 지금 선택으로 받게 될 페이포인트
    public var payPoint: Int {
        switch kind {
        case .step: StepExchangeRate.payPoint(units: units)
        case .starPoint: units      // 별가루는 1:1 이다
        }
    }

    /// 환전 후 남는 잔액
    public var remaining: Int {
        switch kind {
        case .step: available - StepExchangeRate.walkingCount(units: units)
        case .starPoint: available - units
        }
    }

    public var canExchange: Bool { units > 0 && !isLoading }

    // MARK: - 로딩

    public func load() async {
        isLoading = true
        defer { isLoading = false }

        mong = await mongService.currentMong()

        switch kind {
        case .step:
            // 표시용이 아니라 **확정 잔액**을 쓴다. 아직 지갑에 안 들어온 걸음까지
            // 환전하면 배치가 끝내 도착하지 않았을 때 잔액이 음수로 밀린다.
            available = await stepService.currentStep().walkingCount
        case .starPoint:
            await run { self.available = try await self.playerService.refresh().starPoint }
        }
        units = 0
    }

    // MARK: - 단위 선택

    public func increase() { units = min(units + 1, maxUnits) }
    public func decrease() { units = max(units - 1, 0) }

    public func askConfirm() { isConfirming = true }
    public func cancelConfirm() { isConfirming = false }

    // MARK: - 환전

    public func exchange() async {
        guard canExchange else { return }
        isConfirming = false
        isLoading = true
        defer { isLoading = false }

        await run {
            switch self.kind {
            case .step:
                try await self.stepService.exchange(units: self.units)
            case .starPoint:
                guard let mong = self.mong else { throw MongError.noMong }
                try await self.playerService.exchangeStarPoint(mongId: mong.mongId, starPoint: self.units)
            }
            // 페이포인트가 올라갔으므로 몽을 다시 읽는다.
            try await self.mongService.refresh()
            self.didFinish = true
        }
    }

    public func recoverFromError(_ error: any Error) async {
        isConfirming = false
        isLoading = false
    }
}

import Foundation
import MongsModel
import MongsService
import Observation

/// 배틀 메뉴 (매칭 대기)
///
/// Android `pages/battle/BattleMenuViewModel.kt` 이식.
@Observable
@MainActor
public final class BattleMenuViewModel: ErrorReportingViewModel {

    public enum Phase: Sendable, Equatable {
        case loading
        case idle
        /// 대기열에 들어가 상대를 찾는 중
        case matching
        /// 매칭 취소 확인 다이얼로그
        case cancelConfirm
    }

    public private(set) var phase: Phase = .loading
    public private(set) var outcome: MatchOutcome?
    public private(set) var mong: Mong?
    /// 상대를 찾았다. 화면이 이걸 보고 매치로 넘어간다.
    public private(set) var matched: MatchQueue?
    public private(set) var shouldClose = false

    /// 배팅 포인트를 낼 수 있는지.
    public var canEnter: Bool {
        guard let outcome, let mong else { return false }
        return mong.payPoint >= outcome.battingPayPoint
    }

    private let service: BattleService
    private let mongService: MongService
    private var queueTask: Task<Void, Never>?

    public init(service: BattleService, mongService: MongService) {
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
        guard await service.isAvailable else {
            // MQTT 자격증명이 없는 빌드에서는 배틀을 못 한다. 조용히 실패하지 않고 알린다.
            await ErrorBanner.shared.post("실시간 연결을 사용할 수 없어요")
            shouldClose = true
            return
        }

        await run {
            self.outcome = try await self.service.outcome()
            self.phase = .idle
        }
    }

    /// 대기열 등록 + 매칭 대기.
    public func startMatching() async {
        guard phase == .idle, canEnter, let mong else { return }
        phase = .matching

        // 상대가 잡히길 기다린다. 등록보다 **구독을 먼저** 건다 —
        // 반대로 하면 바로 잡힌 매칭을 놓친다.
        queueTask?.cancel()
        let stream = await service.matchQueueStream()
        queueTask = Task { [weak self] in
            for await queue in stream {
                guard let self else { return }
                self.matched = queue
                return
            }
        }

        await run { try await self.service.enterQueue(mongId: mong.mongId) }
    }

    public func askCancel() {
        guard phase == .matching else { return }
        phase = .cancelConfirm
    }

    public func dismissCancel() {
        guard phase == .cancelConfirm else { return }
        phase = .matching
    }

    /// 대기열에서 뺀다.
    public func cancelMatching() async {
        guard let mong else { return }
        queueTask?.cancel()
        queueTask = nil
        await service.leaveQueue(mongId: mong.mongId)
        phase = .idle
    }

    /// 매치 화면으로 넘어간 뒤 호출. 다음 매칭을 위해 상태를 되돌린다.
    public func consumeMatch() {
        matched = nil
        phase = .idle
    }

    public func stop() {
        queueTask?.cancel()
        queueTask = nil
    }

    public func recoverFromError(_ error: any Error) async {
        phase = .idle
    }
}

/// 배틀 매치 (라운드 진행)
///
/// Android `pages/battle/BattleMatchViewModel.kt` 이식.
@Observable
@MainActor
public final class BattleMatchViewModel: ErrorReportingViewModel {

    /// 원본 상수
    private static let effectDelay: Duration = .seconds(2)
    public static let maxRound = 10
    public static let maxSeconds = 30

    public enum Phase: Sendable, Equatable {
        /// 입장 연출
        case entering
        /// 라운드 결과를 보여주는 중
        case idle
        /// 내 선택을 기다린다
        case pick
        /// 상대를 기다린다
        case pickWaiting
        case loading
        case over(MatchWinner)
    }

    public private(set) var phase: Phase = .entering
    public private(set) var match: Match?
    public private(set) var me: Match.Player?
    public private(set) var opponent: Match.Player?
    /// 첫 수신값으로 **한 번만** 고정한다 — HP 바의 분모다.
    public private(set) var myMaxHp: Double = 5000
    public private(set) var opponentMaxHp: Double = 5000
    public private(set) var shouldClose = false

    private let matchId: Int64
    private let playerId: String
    private let deviceId: String
    private let service: BattleService

    private var observeTask: Task<Void, Never>?
    private var maxHpInitialized = false
    /// 정상 종료했는지. 아니면 화면을 떠날 때 퇴장을 알려야 한다.
    private var didFinish = false

    public init(
        matchId: Int64, playerId: String, deviceId: String, service: BattleService
    ) {
        self.matchId = matchId
        self.playerId = playerId
        self.deviceId = deviceId
        self.service = service
    }

    /// 매치 시작. **구독을 먼저 걸고 입장을 알린다.**
    public func start() async {
        phase = .entering

        let matchStream = await service.matchStream(matchId: matchId)
        let overStream = await service.matchOverStream(matchId: matchId)

        observeTask = Task { [weak self] in
            await withTaskGroup(of: Void.self) { group in
                group.addTask {
                    for await match in matchStream {
                        await self?.apply(match)
                    }
                }
                group.addTask {
                    for await match in overStream {
                        await self?.apply(match)
                        await self?.finish()
                        return
                    }
                }
            }
        }

        // 입장 연출이 보일 시간을 준다 (원본 `EFFECT_DELAY`).
        try? await Task.sleep(for: Self.effectDelay)
        await service.enter(matchId: matchId, playerId: playerId)
    }

    private func apply(_ match: Match) async {
        self.match = match
        me = match.me(deviceId: deviceId)
        opponent = match.opponent(deviceId: deviceId)

        if !maxHpInitialized {
            maxHpInitialized = true
            myMaxHp = me?.hp ?? 5000
            opponentMaxHp = opponent?.hp ?? 5000
        }

        guard !match.isLastRound else { return }
        await nextRound()
    }

    /// 라운드 결과를 잠깐 보여준 뒤 다음 선택을 받는다.
    private func nextRound() async {
        phase = .idle
        try? await Task.sleep(for: Self.effectDelay)
        guard !didFinish else { return }
        phase = .pick
    }

    public func pick(_ code: MatchPickCode) async {
        guard phase == .pick, let opponent else { return }
        phase = .idle
        await service.pick(
            matchId: matchId, playerId: playerId,
            targetPlayerId: opponent.playerId, code: code
        )
        phase = .pickWaiting
    }

    /// 매치가 끝났다. 승자를 조회해 결과 화면으로 간다.
    private func finish() async {
        guard !didFinish else { return }
        didFinish = true

        phase = .idle
        try? await Task.sleep(for: Self.effectDelay)
        phase = .loading

        await run {
            let winner = try await self.service.winner(matchId: self.matchId)
            self.phase = .over(winner)
        }
    }

    /// 화면을 떠날 때. **정상 종료가 아니면 퇴장을 알린다** —
    /// 안 보내면 상대가 끝까지 기다린다 (원본 `onCleared`).
    public func leave() async {
        observeTask?.cancel()
        observeTask = nil
        guard !didFinish else { return }
        await service.exit(matchId: matchId, playerId: playerId)
    }

    public func close() { shouldClose = true }

    public func recoverFromError(_ error: any Error) async {
        phase = .pick
    }
}

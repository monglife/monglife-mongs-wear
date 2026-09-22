import Foundation
import MongsModel

/// 실시간 갱신
///
/// Android 는 MQTT 구독을 어댑터마다 흩어 두었다 —
/// `ManagementPersistenceAdapter`(펫), `PlayerPersistenceAdapter`(별가루·슬롯),
/// `DevicePersistenceAdapter`(걸음 복구). 각자 refcount 를 세고 Room 에 쓴다.
///
/// 여기서는 캐시가 이미 각 서비스 안에 있으므로 **구독만 한곳으로 모았다.**
/// 이 액터가 토픽을 만들고, 받은 페이로드를 해당 서비스의 `apply` 로 넘긴다.
/// 서비스들은 MQTT 를 모른 채로 남는다.
public actor RealtimeService {

    private let broker: MQTTBroker
    private let mongService: MongService
    private let playerService: PlayerService
    private let stepService: any StepService
    private let tokenStore: TokenStore
    private let deviceIdentifier: DeviceIdentifierStore

    /// 토픽별로 도는 수신 루프. 같은 토픽을 두 번 열지 않는다.
    private var tasks: [String: Task<Void, Never>] = [:]
    private let decoder = JSONDecoder.mongs()

    public init(
        broker: MQTTBroker,
        mongService: MongService,
        playerService: PlayerService,
        stepService: any StepService,
        tokenStore: TokenStore,
        deviceIdentifier: DeviceIdentifierStore
    ) {
        self.broker = broker
        self.mongService = mongService
        self.playerService = playerService
        self.stepService = stepService
        self.tokenStore = tokenStore
        self.deviceIdentifier = deviceIdentifier
    }

    /// 로그인 직후 시작한다. 계정 단위 토픽(별가루·슬롯)과 기기 토픽(걸음 복구)을 연다.
    public func start() async {
        guard await broker.isConfigured else {
            MongsLog.mqtt("자격증명이 없어 실시간 갱신을 켜지 않는다")
            return
        }
        guard let accountId = await tokenStore.session()?.accountId else { return }
        let deviceId = await deviceIdentifier.identifier()

        listen(RealtimeEvent.StarPoint.self, path: "member/\(accountId)/starPoint") { [playerService] event in
            await playerService.apply(starPoint: event.starPoint)
        }
        listen(RealtimeEvent.SlotCount.self, path: "member/\(accountId)/slotCount") { [playerService] event in
            await playerService.apply(slotCount: event.slotCount)
        }
        listen(RealtimeEvent.StepRestore.self, path: "device/\(deviceId)/step/restore") { [stepService] event in
            await stepService.applyRestore(
                walkingCount: event.restoreWalkingCount,
                eventId: event.eventId
            )
        }
    }

    /// 현재 몽이 바뀔 때마다 부른다. 이전 몽의 구독은 닫고 새 몽을 연다.
    public func observeMong(_ mongId: Int64?) async {
        guard await broker.isConfigured else { return }

        // 몽 토픽만 골라 정리한다. 계정·기기 토픽은 건드리지 않는다.
        let keep = mongId.map { "mong/management/\($0)" }
        for (path, task) in tasks where path.hasPrefix("mong/management/") && path != keep {
            task.cancel()
            tasks[path] = nil
        }

        guard let mongId else { return }
        listen(RealtimeEvent.Management.self, path: "mong/management/\(mongId)") { [mongService] event in
            await mongService.apply(event)
        }
    }

    /// 로그아웃 시. 구독과 연결을 모두 정리한다.
    public func stop() async {
        for task in tasks.values { task.cancel() }
        tasks.removeAll()
        await broker.shutdown()
    }

    // MARK: - 내부

    private func listen<Event: Decodable & Sendable>(
        _ type: Event.Type,
        path: String,
        handle: @escaping @Sendable (Event) async -> Void
    ) {
        guard tasks[path] == nil else { return }

        let topic = broker.topic(path)
        tasks[path] = Task { [broker, decoder] in
            // 스트림이 끝나거나 Task 가 취소되면 broker 쪽 refcount 도 함께 풀린다.
            for await payload in await broker.subscribe(topic: topic) {
                if Task.isCancelled { break }
                do {
                    // HTTP 와 같은 봉투에 담겨 온다.
                    let envelope = try decoder.decode(APIResponse<Event>.self, from: payload)
                    await handle(envelope.result)
                } catch {
                    MongsLog.mqtt("페이로드 해석 실패: \(topic) — \(error)")
                }
            }
        }
    }
}

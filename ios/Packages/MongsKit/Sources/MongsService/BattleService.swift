import Foundation
import MongsModel

/// 배틀
///
/// Android `data/battle-data/*` (662 LOC) 이식.
///
/// **HTTP 와 MQTT 를 함께 쓴다** — 대기열 등록과 보상 조회는 HTTP, 매칭과 라운드 진행은 MQTT다.
/// 토픽 7종 중 3개를 구독하고 4개로 발행한다.
public actor BattleService {

    private let api: APIClient
    private let broker: MQTTBroker
    private let deviceIdentifier: DeviceIdentifierStore
    private let decoder = JSONDecoder.mongs()
    private let encoder = JSONEncoder.mongs()

    public init(api: APIClient, broker: MQTTBroker, deviceIdentifier: DeviceIdentifierStore) {
        self.api = api
        self.broker = broker
        self.deviceIdentifier = deviceIdentifier
    }

    public var isAvailable: Bool {
        get async { await broker.isConfigured }
    }

    // MARK: - 대기열 (HTTP + MQTT 구독)

    /// 대기열에 넣는다. 상대가 잡히면 아래 스트림으로 알려온다.
    public func enterQueue(mongId: Int64) async throws {
        try await api.send(Endpoint(
            host: .gateway,
            method: .post,
            path: "character/battle/queue/\(mongId)"
        ))
    }

    /// 매칭 결과를 기다린다 — `{prefix}/battle/queue/{deviceId}`.
    public func matchQueueStream() async -> AsyncStream<MatchQueue> {
        let deviceId = await deviceIdentifier.identifier()
        return await stream(MatchQueue.self, path: "battle/queue/\(deviceId)")
    }

    /// 대기열에서 뺀다 — `{prefix}/battle/queue/{mongId}` 로 발행한다.
    ///
    /// ⚠️ 구독은 `deviceId`, 발행은 `mongId` 다. 같은 접두사라 헷갈리기 쉽다.
    public func leaveQueue(mongId: Int64) async {
        await publish(path: "battle/queue/\(mongId)", body: EmptyBody())
    }

    // MARK: - 매치 (MQTT)

    /// 라운드마다 갱신되는 매치 상태 — `{prefix}/battle/match/{matchId}`.
    public func matchStream(matchId: Int64) async -> AsyncStream<Match> {
        await stream(Match.self, path: "battle/match/\(matchId)")
    }

    /// 매치 종료 알림 — `{prefix}/battle/match/over/{matchId}`.
    public func matchOverStream(matchId: Int64) async -> AsyncStream<Match> {
        await stream(Match.self, path: "battle/match/over/\(matchId)")
    }

    /// 입장을 알린다.
    public func enter(matchId: Int64, playerId: String) async {
        struct Body: Encodable { let playerId: String }
        await publish(path: "battle/match/enter/\(matchId)", body: Body(playerId: playerId))
    }

    /// 이번 라운드 선택을 보낸다.
    public func pick(
        matchId: Int64, playerId: String, targetPlayerId: String, code: MatchPickCode
    ) async {
        struct Body: Encodable {
            let playerId: String
            let targetPlayerId: String
            let pickCode: String
        }
        await publish(
            path: "battle/match/pick/\(matchId)",
            body: Body(playerId: playerId, targetPlayerId: targetPlayerId, pickCode: code.rawValue)
        )
    }

    /// 도중에 나간다. **화면을 떠날 때 반드시 보낸다** —
    /// 안 보내면 상대가 끝까지 기다린다 (원본도 `onCleared` 에서 보낸다).
    public func exit(matchId: Int64, playerId: String) async {
        struct Body: Encodable { let playerId: String }
        await publish(path: "battle/match/exit/\(matchId)", body: Body(playerId: playerId))
    }

    // MARK: - 보상 (HTTP)

    /// 배팅/보상 포인트. 메뉴에서 보여준다.
    public func outcome() async throws -> MatchOutcome {
        try await api.request(
            Endpoint(host: .gateway, method: .get, path: "character/battle/match")
        )
    }

    /// 승자 조회. 매치가 끝난 뒤 부른다.
    public func winner(matchId: Int64) async throws -> MatchWinner {
        try await api.request(
            Endpoint(host: .gateway, method: .get, path: "character/battle/match/over/\(matchId)")
        )
    }

    // MARK: - 내부

    private struct EmptyBody: Encodable {}

    /// 토픽 하나를 구독해 봉투를 벗긴 값만 흘려보낸다.
    private func stream<T: Decodable & Sendable>(
        _ type: T.Type, path: String
    ) async -> AsyncStream<T> {
        let topic = broker.topic(path)
        let payloads = await broker.subscribe(topic: topic)
        let (stream, continuation) = AsyncStream<T>.makeStream()

        let task = Task { [decoder] in
            for await payload in payloads {
                do {
                    let envelope = try decoder.decode(APIResponse<T>.self, from: payload)
                    continuation.yield(envelope.result)
                } catch {
                    // 해석 실패는 **원인을 알아야 고칠 수 있다.** 모델과 서버 페이로드가
                    // 어긋나면 화면이 조용히 멈추기만 해서(라운드가 안 와서) 찾기 어렵다.
                    MongsLog.mqtt("배틀 페이로드 해석 실패: \(topic) — \(error)")
                    #if DEBUG
                    MongsLog.mqtt("원문: \(String(decoding: payload, as: UTF8.self))")
                    #endif
                }
            }
            continuation.finish()
        }
        continuation.onTermination = { _ in task.cancel() }
        return stream
    }

    private func publish(path: String, body: some Encodable) async {
        guard let data = try? encoder.encode(body) else { return }
        do {
            try await broker.publish(topic: broker.topic(path), payload: data)
        } catch {
            MongsLog.mqtt("배틀 발행 실패: \(path) — \(error)")
        }
    }
}

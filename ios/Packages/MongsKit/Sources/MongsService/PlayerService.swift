import Foundation
import MongsModel

/// 플레이어 서비스
///
/// Android `data/member-data/.../player/web/adapter/PlayerWebAdapter.kt` +
/// `persistence/adapter/PlayerPersistenceAdapter.kt` 이식.
///
/// 슬롯 수와 별가루는 MQTT 로도 밀려 들어오는 값이라(`member/{accountId}/slotCount`,
/// `.../starPoint`) 캐시를 단일 출처로 두고 스트림으로 내보낸다.
/// MQTT 를 붙이면 그 푸시가 같은 캐시로 들어온다.
public actor PlayerService {

    private let api: APIClient
    private var cached: Player?
    private var continuations: [UUID: AsyncStream<Player?>.Continuation] = [:]

    public init(api: APIClient) {
        self.api = api
    }

    public func currentPlayer() -> Player? { cached }

    public func playerStream() -> AsyncStream<Player?> {
        let (stream, continuation) = AsyncStream<Player?>.makeStream()
        let id = UUID()
        continuations[id] = continuation
        continuation.yield(cached)

        continuation.onTermination = { [weak self] _ in
            Task { await self?.removeContinuation(id) }
        }
        return stream
    }

    /// 플레이어 정보를 서버에서 받아온다.
    @discardableResult
    public func refresh() async throws -> Player {
        let player: Player = try await api.request(
            Endpoint(host: .gateway, method: .get, path: "user/player")
        )
        return publish(player)
    }

    /// 플레이어 등록. 계정에 플레이어가 없을 때 한 번 부른다.
    public func create() async throws {
        try await api.send(Endpoint(host: .gateway, method: .post, path: "user/player"))
    }

    /// 슬롯 구매.
    ///
    /// 응답이 갱신된 슬롯 수와 별가루를 함께 주므로 따로 조회하지 않는다.
    @discardableResult
    public func buySlot() async throws -> Player {
        let player: Player = try await api.request(
            Endpoint(host: .gateway, method: .patch, path: "user/player/slot")
        )
        return publish(player)
    }

    /// MQTT 푸시를 반영한다. 서버가 슬롯 수/별가루만 보내므로 나머지는 유지한다.
    public func apply(slotCount: Int? = nil, starPoint: Int? = nil) {
        guard let current = cached else { return }
        publish(Player(
            accountId: current.accountId,
            slotCount: slotCount ?? current.slotCount,
            starPoint: starPoint ?? current.starPoint
        ))
    }

    @discardableResult
    private func publish(_ player: Player) -> Player {
        cached = player
        for continuation in continuations.values {
            continuation.yield(player)
        }
        return player
    }

    private func removeContinuation(_ id: UUID) {
        continuations[id] = nil
    }
}

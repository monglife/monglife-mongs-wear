import Foundation
import MongsModel

/// 몽 관리 서비스
///
/// Android `data/mong-data/.../web/adapter/ManagementWebAdapter.kt` (246 LOC) +
/// `persistence/adapter/ManagementPersistenceAdapter.kt` (185 LOC) 를 합친 것.
///
/// **캐시가 화면의 단일 출처다.** 서버 응답이든 (나중에 붙을) MQTT 푸시든 전부 캐시로
/// 들어오고, 화면은 캐시 스트림만 본다. Android 가 Room 을 그 자리에 둔 것과 같은 구조인데,
/// 캐시할 대상이 현재 몽 하나뿐이라 DB 를 들일 이유가 없다.
public actor MongService {

    private let api: APIClient
    private let cache: MongCache
    private var continuations: [UUID: AsyncStream<Mong?>.Continuation] = [:]

    public init(api: APIClient, cache: MongCache) {
        self.api = api
        self.cache = cache
    }

    // MARK: - 조회

    /// 현재 몽 스트림. 구독 즉시 캐시된 값이 흘러온다.
    ///
    /// Android `ObserveCurrentMongUseCase` 대응.
    public func currentMongStream() async -> AsyncStream<Mong?> {
        let (stream, continuation) = AsyncStream<Mong?>.makeStream()
        let id = UUID()
        continuations[id] = continuation

        continuation.yield(await cache.mong())

        continuation.onTermination = { [weak self] _ in
            Task { await self?.removeContinuation(id) }
        }
        return stream
    }

    public func currentMong() async -> Mong? {
        await cache.mong()
    }

    /// 서버에서 몽 목록을 받아 현재 몽을 갱신한다.
    ///
    /// Android 는 슬롯이 여러 개고 `currentMongId` 로 고른다. 여기서는 v1 범위대로
    /// **첫 번째 살아 있는 몽**을 현재 몽으로 삼는다. 슬롯 선택은 SlotPick 을 붙일 때 온다.
    @discardableResult
    public func refresh() async throws -> Mong? {
        let responses: [MongResponse.Full] = try await api.request(
            Endpoint(host: .gateway, method: .get, path: "character/management")
        )

        let next = responses
            .map(\.mong)
            .first { $0.stateCode != .delete }

        return await store(next)
    }

    // MARK: - 생성 / 삭제

    @discardableResult
    public func create(name: String, sleepAt: Date, wakeupAt: Date) async throws -> Mong {
        let response: MongResponse.Full = try await api.request(try Endpoint.json(
            host: .gateway,
            method: .post,
            path: "character/management",
            body: CreateMongRequest(name: name, sleepAt: sleepAt, wakeupAt: wakeupAt)
        ))
        let mong = response.mong
        await store(mong)
        return mong
    }

    public func delete(mongId: Int64) async throws {
        let _: MongResponse.IdOnly = try await api.request(
            Endpoint(host: .gateway, method: .delete, path: "character/management/\(mongId)")
        )
        await store(nil)
    }

    // MARK: - 상호작용
    //
    // 응답이 부분 필드만 주므로 캐시된 몽에 병합한다.
    // 캐시가 비어 있으면(= 몽이 없는데 버튼이 눌린 비정상 상황) 병합할 대상이 없으니
    // 서버에서 다시 읽는다.

    @discardableResult
    public func stroke() async throws -> Mong? {
        try await interact(MongResponse.Stroke.self, method: .post, path: "stroke") { $0.applying($1) }
    }

    @discardableResult
    public func toggleSleep() async throws -> Mong? {
        try await interact(MongResponse.Sleep.self, method: .put, path: "sleep") { $0.applying($1) }
    }

    @discardableResult
    public func cleanPoop() async throws -> Mong? {
        try await interact(MongResponse.PoopClean.self, method: .post, path: "poopClean") { $0.applying($1) }
    }

    @discardableResult
    public func evolve() async throws -> Mong? {
        try await interact(MongResponse.Evolution.self, method: .put, path: "evolution") { $0.applying($1) }
    }

    /// 졸업. 몽이 떠나므로 캐시를 비운다.
    public func graduate() async throws {
        guard let mong = await cache.mong() else { throw MongError.noMong }

        let _: MongResponse.IdOnly = try await api.request(
            Endpoint(host: .gateway, method: .put, path: "character/management/graduate/\(mong.mongId)")
        )
        await store(nil)
    }

    // MARK: - 내부

    private func interact<Response: Decodable & Sendable>(
        _ type: Response.Type,
        method: Endpoint.Method,
        path: String,
        merge: (Mong, Response) -> Mong
    ) async throws -> Mong? {
        guard let mong = await cache.mong() else { throw MongError.noMong }

        let response: Response = try await api.request(
            Endpoint(host: .gateway, method: method, path: "character/management/\(path)/\(mong.mongId)")
        )

        // 요청이 도는 사이 MQTT 나 새로고침이 캐시를 갈아치웠을 수 있다.
        // 그때는 낡은 스냅샷이 아니라 최신 값에 병합해야 한다.
        let base = await cache.mong() ?? mong
        return await store(merge(base, response))
    }

    @discardableResult
    private func store(_ mong: Mong?) async -> Mong? {
        await cache.save(mong)
        for continuation in continuations.values {
            continuation.yield(mong)
        }
        return mong
    }

    private func removeContinuation(_ id: UUID) {
        continuations[id] = nil
    }
}

/// 몽 관련 오류
public enum MongError: Error, Equatable, Sendable {
    /// 몽이 없는데 상호작용을 시도했다
    case noMong

    public var message: String {
        switch self {
        case .noMong: "몽이 없습니다."
        }
    }

    public var isMessageShown: Bool { true }
}

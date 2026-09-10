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

    /// 몽 목록 전체를 조회한다. 슬롯 화면이 쓴다.
    ///
    /// 목록에는 죽은 몽·졸업한 몽도 들어 있다 — 슬롯 화면이 그 상태를 보여주고
    /// 삭제/졸업 버튼을 띄워야 하므로 걸러내지 않는다.
    public func allMongs() async throws -> [Mong] {
        let responses: [MongResponse.Full] = try await api.request(
            Endpoint(host: .gateway, method: .get, path: "character/management")
        )
        return responses.map(\.mong)
    }

    /// 현재 몽을 지정한다.
    ///
    /// Android 는 `currentMongId` 를 DataStore 에 저장하고 Room 에서 그 몽을 읽는다.
    /// 여기서는 캐시가 곧 현재 몽이라 캐시를 갈아끼우는 것으로 끝난다.
    public func select(_ mong: Mong) async {
        await store(mong)
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

    /// MQTT 푸시를 캐시에 반영한다.
    ///
    /// **다른 몽의 이벤트는 버린다.** 슬롯을 바꾼 직후엔 이전 몽의 구독이 유예(5초) 동안
    /// 살아 있어서 남의 상태가 도착할 수 있다.
    public func apply(_ event: RealtimeEvent.Management) async {
        guard let current = await cache.mong(), current.mongId == event.mongId else { return }
        await store(current.applying(event))
    }

    // MARK: - 먹이 / 인벤토리

    /// 먹이 목록 조회.
    ///
    /// 서버가 밥과 간식을 다른 경로로 주고 필드명도 다르다(`foodCode` / `snackCode`).
    /// 화면에서는 같은 목록이라 여기서 하나로 맞춘다.
    public func feedItems(kind: FeedItem.Kind) async throws -> [FeedItem] {
        guard let mong = await cache.mong() else { throw MongError.noMong }
        let path = "character/interaction/\(kind.path)/\(mong.mongId)"
        let endpoint = Endpoint(host: .gateway, method: .get, path: path)

        switch kind {
        case .food:
            let list: [MongResponse.FoodList] = try await api.request(endpoint)
            return list.map(\.item)
        case .snack:
            let list: [MongResponse.SnackList] = try await api.request(endpoint)
            return list.map(\.item)
        }
    }

    /// 먹이 주기. 응답이 바뀐 스탯을 주므로 캐시에 병합한다.
    @discardableResult
    public func feed(_ item: FeedItem) async throws -> Mong? {
        guard let mong = await cache.mong() else { throw MongError.noMong }

        // 요청 본문의 키 이름이 종류마다 다르다.
        let body = [item.kind.codeField: item.code]
        let response: MongResponse.Consume = try await api.request(try Endpoint.json(
            host: .gateway,
            method: .post,
            path: "character/interaction/\(item.kind.path)/\(mong.mongId)",
            body: body
        ))

        let base = await cache.mong() ?? mong
        return await store(base.applying(response))
    }

    /// 인벤토리 목록 조회. 서버가 페이징으로 준다.
    ///
    /// `page` 는 **1-based** 다 — Android `InventoryViewModel.INIT_PAGE = 1` 과 맞춘다.
    public func inventory(page: Int = 1, size: Int = 4) async throws -> InventoryPage {
        guard let mong = await cache.mong() else { throw MongError.noMong }
        let response: APIPageResponse<InventoryItem> = try await api.requestPage(
            Endpoint(
                host: .gateway,
                method: .get,
                path: "character/interaction/inventory/\(mong.mongId)",
                query: ["page": "\(page)", "size": "\(size)"]
            )
        )
        return InventoryPage(
            items: response.result,
            page: response.page ?? page,
            totalPage: response.totalPage ?? 0,
            isLastPage: response.isLastPage ?? true
        )
    }

    /// 인벤토리 항목 사용.
    @discardableResult
    public func useInventory(_ item: InventoryItem) async throws -> Mong? {
        guard let mong = await cache.mong() else { throw MongError.noMong }

        struct Request: Encodable { let inventoryId: Int64 }
        let response: MongResponse.Consume = try await api.request(try Endpoint.json(
            host: .gateway,
            method: .post,
            path: "character/interaction/inventory/\(mong.mongId)",
            body: Request(inventoryId: item.inventoryId)
        ))

        let base = await cache.mong() ?? mong
        return await store(base.applying(response))
    }

    // MARK: - 랜덤 뽑기

    /// 뽑기권 구매. 페이포인트를 쓴다.
    @discardableResult
    public func buyRandomDrawTicket() async throws -> Mong? {
        guard let mong = await cache.mong() else { throw MongError.noMong }
        let response: MongResponse.RandomDrawTicket = try await api.request(
            Endpoint(
                host: .gateway,
                method: .post,
                path: "character/interaction/randomDraw/ticket/\(mong.mongId)"
            )
        )
        let base = await cache.mong() ?? mong
        return await store(base.applying(response))
    }

    /// 뽑기. 티켓을 하나 쓰고 결과를 돌려준다.
    ///
    /// 응답이 몽 상태를 주지 않으므로 **티켓 수는 여기서 직접 깎는다.**
    /// 서버가 다음 조회에서 정확한 값을 다시 알려준다.
    public func randomDraw() async throws -> RandomDrawResult {
        guard let mong = await cache.mong() else { throw MongError.noMong }
        let result: RandomDrawResult = try await api.request(
            Endpoint(
                host: .gateway,
                method: .post,
                path: "character/interaction/randomDraw/\(mong.mongId)"
            )
        )
        if var next = await cache.mong(), next.randomDrawTicketCount > 0 {
            next.randomDrawTicketCount -= 1
            await store(next)
        }
        return result
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

import Foundation
import MongsModel
import Testing
@testable import MongsService

@Suite("몽 서비스")
struct MongServiceTests {

    private func makeConfig() -> AppConfig {
        AppConfig(
            profile: .dev,
            discoveryAPIURL: URL(string: "https://discovery.test/api/")!,
            gatewayAPIURL: URL(string: "https://gateway.test/api/")!,
            mqttURL: "tcp://mqtt.test:1883", mqttTopic: "mongs-dev", mqttKeepAlive: 180,
            connectTimeout: 120, readTimeout: 30, writeTimeout: 10
        )
    }

    private func makeService(_ transport: StubTransport) -> (MongService, MongCache) {
        let tokenStore = TokenStore(store: InMemorySecureStore())
        let api = APIClient(config: makeConfig(), tokenStore: tokenStore, transport: transport)
        let cache = MongCache(store: InMemoryKeyValueStore())
        return (MongService(api: api, cache: cache), cache)
    }

    private let fullMong = """
    {"mongId":7,"name":"뭉치","mongCode":"CH100","mongName":"몽","stateCode":"NORMAL",
     "statusCode":"NORMAL","level":1,"sleepAt":"22:00:00","wakeupAt":"07:00:00",
     "payPoint":150,"isSleep":false,"strengthRatio":10,"healthyRatio":20,
     "satietyRatio":30,"fatigueRatio":40,"expRatio":50,"weight":3.5,
     "poopCount":3,"randomDrawTicketCount":0,
     "createdAt":"2026-03-04T05:06:07","updatedAt":"2026-03-04T05:06:08"}
    """

    @Test("목록을 받아 첫 번째 몽을 현재 몽으로 삼는다")
    func refreshPicksFirstMong() async throws {
        let transport = StubTransport()
        transport.stub("character/management", [.ok(#"{"result":[\#(fullMong)]}"#)])
        let (service, cache) = makeService(transport)

        let mong = try await service.refresh()

        #expect(mong?.mongId == 7)
        #expect(await cache.mong()?.name == "뭉치")
    }

    @Test("삭제 상태인 몽은 건너뛴다")
    func refreshSkipsDeleted() async throws {
        let deleted = fullMong.replacingOccurrences(of: #""stateCode":"NORMAL""#, with: #""stateCode":"DELETE""#)
            .replacingOccurrences(of: #""mongId":7"#, with: #""mongId":1"#)
        let transport = StubTransport()
        transport.stub("character/management", [.ok(#"{"result":[\#(deleted),\#(fullMong)]}"#)])
        let (service, _) = makeService(transport)

        let mong = try await service.refresh()

        #expect(mong?.mongId == 7)
    }

    @Test("쓰다듬기 응답을 캐시된 몽에 병합한다")
    func strokeMergesIntoCache() async throws {
        let transport = StubTransport()
        transport.stub("character/management/stroke", [.ok("""
        {"result":{"mongId":7,"expRatio":88,"createdAt":"2026-03-04T05:06:07","updatedAt":"2026-03-04T05:06:09"}}
        """)])
        let (service, cache) = makeService(transport)
        await cache.save(try decodeMong())

        let next = try await service.stroke()

        #expect(next?.expRatio == 88)
        // 응답에 없던 필드는 캐시 값이 유지돼야 한다
        #expect(next?.poopCount == 3)
        #expect(next?.name == "뭉치")
    }

    @Test("몽이 없으면 상호작용이 실패한다")
    func interactionRequiresMong() async {
        let transport = StubTransport()
        let (service, _) = makeService(transport)

        await #expect(throws: MongError.noMong) {
            _ = try await service.stroke()
        }
        // 서버를 부르지 않아야 한다
        #expect(transport.requests.isEmpty)
    }

    @Test("졸업하면 캐시가 비워진다")
    func graduateClearsCache() async throws {
        let transport = StubTransport()
        transport.stub("character/management/graduate", [.ok(#"{"result":{"mongId":7}}"#)])
        let (service, cache) = makeService(transport)
        await cache.save(try decodeMong())

        try await service.graduate()

        #expect(await cache.mong() == nil)
    }

    @Test("상호작용은 올바른 HTTP 메서드와 경로를 쓴다")
    func interactionUsesCorrectVerbs() async throws {
        let transport = StubTransport()
        transport.stub("character/management/sleep", [.ok("""
        {"result":{"mongId":7,"isSleep":true,"createdAt":"2026-03-04T05:06:07","updatedAt":"2026-03-04T05:06:08"}}
        """)])
        let (service, cache) = makeService(transport)
        await cache.save(try decodeMong())

        _ = try await service.toggleSleep()

        let request = try #require(transport.requests.first)
        #expect(request.httpMethod == "PUT")
        #expect(request.url?.absoluteString == "https://gateway.test/api/character/management/sleep/7")
    }

    @Test("스트림은 구독 즉시 캐시 값을 흘려준다")
    func streamEmitsCachedValueImmediately() async throws {
        let transport = StubTransport()
        let (service, cache) = makeService(transport)
        await cache.save(try decodeMong())

        var iterator = await service.currentMongStream().makeAsyncIterator()
        let first = await iterator.next()

        #expect(first??.mongId == 7)
    }

    private func decodeMong() throws -> Mong {
        try JSONDecoder.mongs().decode(MongResponse.Full.self, from: Data(fullMong.utf8)).mong
    }
}

@Suite("몽 캐시")
struct MongCacheTests {

    @Test("앱을 다시 켜도 마지막 몽이 남는다")
    func persistsAcrossInstances() async throws {
        // 오프라인에서도 마지막 몽을 보여주기 위한 것이다.
        let store = InMemoryKeyValueStore()
        let mong = Mong(
            mongId: 3, name: "테스트", mongCode: "CH100", mongName: "몽",
            stateCode: .normal, statusCode: .normal, level: 1,
            sleepAt: Date(timeIntervalSince1970: 0), wakeupAt: Date(timeIntervalSince1970: 0),
            payPoint: 0, isSleep: false, strengthRatio: 0, healthyRatio: 0,
            satietyRatio: 0, fatigueRatio: 0, expRatio: 0, weight: 0,
            poopCount: 0, randomDrawTicketCount: 0,
            createdAt: Date(timeIntervalSince1970: 0), updatedAt: Date(timeIntervalSince1970: 0)
        )
        await MongCache(store: store).save(mong)

        #expect(await MongCache(store: store).mong()?.mongId == 3)
    }

    @Test("깨진 캐시는 없는 것으로 본다")
    func corruptedCacheDegradesToNil() async {
        // 모델이 바뀌면 디코딩이 깨진다. 다음 refresh 가 서버에서 다시 채운다.
        let store = InMemoryKeyValueStore()
        store.set(Data("이건 몽이 아니다".utf8), forKey: "mongs.mong.current.v1")

        #expect(await MongCache(store: store).mong() == nil)
    }
}

import Foundation
import MongsModel
import Testing
@testable import MongsService

@Suite("API 클라이언트")
struct APIClientTests {

    private struct Player: Decodable, Sendable, Equatable {
        let accountId: Int64
    }

    private func makeConfig() -> AppConfig {
        AppConfig(
            profile: .dev,
            discoveryAPIURL: URL(string: "https://discovery.test/api/")!,
            gatewayAPIURL: URL(string: "https://gateway.test/api/")!,
            mqttURL: "tcp://mqtt.test:1883",
            mqttTopic: "mongs-dev",
            mqttKeepAlive: 180,
            connectTimeout: 120,
            readTimeout: 30,
            writeTimeout: 10
        )
    }

    private func makeClient(
        transport: StubTransport,
        session: Session? = nil
    ) async -> (APIClient, TokenStore) {
        let tokenStore = TokenStore(store: InMemorySecureStore())
        if let session { await tokenStore.save(session) }
        let client = APIClient(config: makeConfig(), tokenStore: tokenStore, transport: transport)
        return (client, tokenStore)
    }

    private let playerEndpoint = Endpoint(host: .gateway, method: .get, path: "user/player")

    // MARK: - 기본 동작

    @Test("gateway 요청에 Bearer 토큰을 붙인다")
    func attachesBearerToken() async throws {
        let transport = StubTransport()
        transport.stub("user/player", [.ok(#"{"result":{"accountId":7}}"#)])
        let (client, _) = await makeClient(
            transport: transport,
            session: Session(accountId: 7, accessToken: "ACCESS", refreshToken: "REFRESH")
        )

        let player: Player = try await client.request(playerEndpoint)

        #expect(player.accountId == 7)
        #expect(transport.requests.first?.authorizationHeader == "Bearer ACCESS")
    }

    @Test("인증이 필요 없는 요청에는 토큰을 붙이지 않는다")
    func omitsTokenWhenNotRequired() async throws {
        let transport = StubTransport()
        transport.stub("public/auth/verify", [.ok(#"{"result":{"accountId":0}}"#)])
        let (client, _) = await makeClient(
            transport: transport,
            session: Session(accountId: 7, accessToken: "ACCESS", refreshToken: "R")
        )

        _ = try await client.request(
            Endpoint(host: .discovery, method: .get, path: "public/auth/verify/version",
                     requiresAuthorization: false),
            as: Player.self
        )

        #expect(transport.requests.first?.authorizationHeader == nil)
    }

    @Test("host 에 따라 베이스 URL 이 갈린다")
    func routesToCorrectHost() async throws {
        let transport = StubTransport()
        transport.stub("user/player", [.ok(#"{"result":{"accountId":1}}"#)])
        let (client, _) = await makeClient(
            transport: transport,
            session: Session(accountId: 1, accessToken: "a", refreshToken: "r")
        )

        _ = try await client.request(playerEndpoint, as: Player.self)

        let url = try #require(transport.requests.first?.url?.absoluteString)
        #expect(url == "https://gateway.test/api/user/player")
    }

    // MARK: - 오류 매핑

    @Test("403 은 인가 실패다")
    func mapsForbidden() async {
        let transport = StubTransport()
        transport.stub("user/player", [.status(403)])
        let (client, _) = await makeClient(
            transport: transport,
            session: Session(accountId: 1, accessToken: "a", refreshToken: "r")
        )

        await #expect(throws: APIError.unauthorized) {
            _ = try await client.request(playerEndpoint, as: Player.self)
        }
    }

    @Test("500 은 서버 연결 실패다")
    func mapsServerError() async {
        let transport = StubTransport()
        transport.stub("user/player", [.status(503)])
        let (client, _) = await makeClient(
            transport: transport,
            session: Session(accountId: 1, accessToken: "a", refreshToken: "r")
        )

        await #expect(throws: APIError.serverUnreachable(statusCode: 503)) {
            _ = try await client.request(playerEndpoint, as: Player.self)
        }
    }

    @Test("오류 봉투의 서버 코드를 꺼낸다")
    func extractsServerErrorCode() async throws {
        // 로그인 플로우가 이 코드로 "가입 후 재로그인"을 판단한다.
        let transport = StubTransport()
        transport.stub("public/auth/login", [
            .status(400, #"{"code":"DISCOVERY-ACCOUNT-101","message":"미가입","httpStatus":400}"#),
        ])
        let (client, _) = await makeClient(transport: transport)

        let endpoint = Endpoint(host: .discovery, method: .post, path: "public/auth/login",
                                requiresAuthorization: false)

        do {
            _ = try await client.request(endpoint, as: Player.self)
            Issue.record("오류가 나야 한다")
        } catch let error as APIError {
            #expect(error.isNeedJoin)
            #expect(error.serverCode == "DISCOVERY-ACCOUNT-101")
        }
    }

    // MARK: - 401 재발급

    @Test("401 이면 재발급 후 새 토큰으로 재시도한다")
    func refreshesOnUnauthenticated() async throws {
        let transport = StubTransport()
        // 첫 호출 401, 재발급 후 두 번째 호출 성공
        transport.stub("user/player", [
            .status(401),
            .ok(#"{"result":{"accountId":7}}"#),
        ])
        transport.stub("public/auth/reissue", [
            .ok(#"{"result":{"accessToken":"NEW_ACCESS","refreshToken":"NEW_REFRESH"}}"#),
        ])

        let (client, tokenStore) = await makeClient(
            transport: transport,
            session: Session(accountId: 7, accessToken: "OLD", refreshToken: "R", version: 0)
        )

        let player: Player = try await client.request(playerEndpoint)

        #expect(player.accountId == 7)
        #expect(transport.hits("user/player") == 2)

        // 재시도 요청에는 새 토큰이 실려야 한다
        let retried = transport.requests.last { $0.url?.path.contains("user/player") == true }
        #expect(retried?.authorizationHeader == "Bearer NEW_ACCESS")

        // 저장된 세션도 갱신되고 세대가 올라간다
        let saved = await tokenStore.session()
        #expect(saved?.accessToken == "NEW_ACCESS")
        #expect(saved?.version == 1)
    }

    @Test("재발급이 실패하면 세션을 지우고 인가 실패로 끝낸다")
    func clearsSessionWhenReissueFails() async {
        let transport = StubTransport()
        transport.stub("user/player", [.status(401)])
        transport.stub("public/auth/reissue", [.status(401)])

        let (client, tokenStore) = await makeClient(
            transport: transport,
            session: Session(accountId: 7, accessToken: "OLD", refreshToken: "R")
        )

        await #expect(throws: APIError.unauthorized) {
            _ = try await client.request(playerEndpoint, as: Player.self)
        }

        #expect(await tokenStore.session() == nil)
    }

    @Test("동시에 401 이 여러 개 터져도 재발급은 한 번만 한다")
    func refreshIsSingleFlight() async throws {
        // 이게 version 카운터가 존재하는 이유다. 재발급이 여러 번 나가면
        // refreshToken 이 중복 소모되어 서버가 세션을 끊는다.
        let transport = StubTransport()
        transport.stub("user/player", [
            .status(401), .status(401), .status(401), .status(401),
            .ok(#"{"result":{"accountId":7}}"#),
        ])
        transport.stub("public/auth/reissue", [
            .ok(#"{"result":{"accessToken":"NEW_ACCESS","refreshToken":"NEW_REFRESH"}}"#),
        ])

        let (client, _) = await makeClient(
            transport: transport,
            session: Session(accountId: 7, accessToken: "OLD", refreshToken: "R")
        )

        await withTaskGroup(of: Void.self) { group in
            for _ in 0 ..< 4 {
                group.addTask {
                    _ = try? await client.request(self.playerEndpoint, as: Player.self)
                }
            }
        }

        #expect(transport.hits("public/auth/reissue") == 1)
    }

    @Test("재발급 요청 자체에는 토큰을 붙이지 않는다")
    func reissueGoesUnauthenticated() async throws {
        // 붙이면 그 요청도 401 → 재발급 → 무한 재귀가 된다.
        let transport = StubTransport()
        transport.stub("user/player", [.status(401), .ok(#"{"result":{"accountId":7}}"#)])
        transport.stub("public/auth/reissue", [
            .ok(#"{"result":{"accessToken":"NEW","refreshToken":"NEW_R"}}"#),
        ])

        let (client, _) = await makeClient(
            transport: transport,
            session: Session(accountId: 7, accessToken: "OLD", refreshToken: "R")
        )

        _ = try await client.request(playerEndpoint, as: Player.self)

        let reissueRequest = try #require(
            transport.requests.first { $0.url?.path.contains("reissue") == true }
        )
        #expect(reissueRequest.authorizationHeader == nil)
        #expect(reissueRequest.url?.absoluteString == "https://discovery.test/api/public/auth/reissue")
    }
}

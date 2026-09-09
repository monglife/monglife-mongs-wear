import Foundation
import MongsModel
import Testing
@testable import MongsService

/// Apple 로그인 스텁
private struct StubSignIn: AppleSignInProviding {
    let result: Result<AppleCredential, AuthError>

    func signIn() async throws -> AppleCredential {
        try result.get()
    }

    static let credential = AppleCredential(
        userIdentifier: "001234.abcdef",
        identityToken: "eyJhbGciOiJSUzI1NiJ9.payload.sig",
        email: "user@privaterelay.appleid.com",
        fullName: "홍 길동"
    )
}

@Suite("인증 서비스")
struct AuthServiceTests {

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

    private func makeService(
        transport: StubTransport,
        signIn: any AppleSignInProviding = StubSignIn(result: .success(StubSignIn.credential))
    ) -> (AuthService, TokenStore) {
        // 로그인은 기기 등록을 먼저 탄다. 개별 테스트마다 적지 않도록 여기서 깐다.
        transport.stub("public/userDevice", [.ok(#"{"result":{}}"#)])

        let tokenStore = TokenStore(store: InMemorySecureStore())
        let api = APIClient(config: makeConfig(), tokenStore: tokenStore, transport: transport)
        let service = AuthService(
            api: api,
            tokenStore: tokenStore,
            signInClient: signIn,
            identity: ClientIdentity(
                deviceId: "DEVICE-1",
                appPackageName: "com.mongs.wear",
                deviceName: "Apple Watch",
                buildVersion: "0.1.0"
            )
        )
        return (service, tokenStore)
    }

    private let loginOK = #"{"result":{"accountId":9,"accessToken":"A","refreshToken":"R"}}"#

    @Test("로그인하면 세션이 저장된다")
    func signInStoresSession() async throws {
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [.ok(loginOK)])
        let (service, tokenStore) = makeService(transport: transport)

        let session = try await service.signIn()

        #expect(session.accountId == 9)
        #expect(session.accessToken == "A")
        #expect(session.version == 0)
        #expect(await tokenStore.session() == session)
    }

    @Test("미가입이면 가입 후 다시 로그인한다")
    func joinsThenLoginsWhenAccountMissing() async throws {
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [
            .status(400, #"{"code":"DISCOVERY-ACCOUNT-101","httpStatus":400}"#),
            .ok(loginOK),
        ])
        transport.stub("public/auth/join/apple", [.ok(#"{"result":{}}"#)])

        let (service, _) = makeService(transport: transport)

        let session = try await service.signIn()

        #expect(session.accountId == 9)
        #expect(transport.hits("public/auth/join/apple") == 1)
        #expect(transport.hits("public/auth/login/apple") == 2)
    }

    @Test("로그인 요청에 기기 정보와 identity token 이 실린다")
    func loginCarriesIdentity() async throws {
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [.ok(loginOK)])
        let (service, _) = makeService(transport: transport)

        _ = try await service.signIn()

        let body = try #require(
            transport.requests.first { $0.url?.path.contains("login/apple") == true }?.httpBody
        )
        let json = try #require(try JSONSerialization.jsonObject(with: body) as? [String: Any])

        #expect(json["socialAccountId"] as? String == "001234.abcdef")
        #expect(json["identityToken"] as? String == StubSignIn.credential.identityToken)
        #expect(json["deviceId"] as? String == "DEVICE-1")
        #expect(json["appPackageName"] as? String == "com.mongs.wear")
    }

    @Test("가입 요청에는 이메일과 이름이 실린다")
    func joinCarriesProfile() async throws {
        // Apple 은 최초 인증 때만 이메일/이름을 준다. 그때 서버로 넘겨야 한다.
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [
            .status(400, #"{"code":"DISCOVERY-ACCOUNT-101","httpStatus":400}"#),
            .ok(loginOK),
        ])
        transport.stub("public/auth/join/apple", [.ok(#"{"result":{}}"#)])
        let (service, _) = makeService(transport: transport)

        _ = try await service.signIn()

        let body = try #require(
            transport.requests.first { $0.url?.path.contains("join/apple") == true }?.httpBody
        )
        let json = try #require(try JSONSerialization.jsonObject(with: body) as? [String: Any])

        #expect(json["email"] as? String == "user@privaterelay.appleid.com")
        #expect(json["name"] as? String == "홍 길동")
    }

    @Test("사용자가 로그인을 취소하면 서버를 부르지 않는다")
    func canceledSignInSkipsServer() async {
        let transport = StubTransport()
        let (service, tokenStore) = makeService(
            transport: transport,
            signIn: StubSignIn(result: .failure(.canceled))
        )

        await #expect(throws: AuthError.canceled) {
            _ = try await service.signIn()
        }

        #expect(transport.requests.isEmpty)
        #expect(await tokenStore.session() == nil)
    }

    @Test("로그아웃은 서버가 실패해도 로컬 세션을 지운다")
    func signOutClearsLocalSessionEvenOnServerFailure() async {
        let transport = StubTransport()
        transport.stub("public/auth/logout", [.status(500)])
        let (service, tokenStore) = makeService(transport: transport)
        await tokenStore.save(Session(accountId: 9, accessToken: "A", refreshToken: "R"))

        await service.signOut()

        #expect(await tokenStore.session() == nil)
    }

    @Test("로그인 전에 기기를 먼저 등록한다")
    func registersDeviceBeforeLogin() async throws {
        // ⚠️ 순서가 계약이다. 등록되지 않은 deviceId 로 로그인하면 서버가
        // DISCOVERY-DEVICE-101 을 준다. Android LoginUseCase 도 같은 순서다.
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [.ok(loginOK)])
        let (service, _) = makeService(transport: transport)

        _ = try await service.signIn()

        let paths = transport.requests.compactMap { $0.url?.path }
        let deviceIndex = try #require(paths.firstIndex { $0.contains("userDevice") })
        let loginIndex = try #require(paths.firstIndex { $0.contains("login/apple") })
        #expect(deviceIndex < loginIndex)
    }

    @Test("기기 등록에 iOS 자리표시자 푸시 토큰이 실린다")
    func deviceRegistrationCarriesPlaceholderToken() async throws {
        // 서버가 fcmToken 을 @NotBlank 로 막아 두어 빈 값을 못 보낸다.
        // iOS 는 FCM 을 쓰지 않으므로 자리표시자를 보낸다. APNs 를 붙일 때 교체한다.
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [.ok(loginOK)])
        let (service, _) = makeService(transport: transport)

        _ = try await service.signIn()

        let body = try #require(
            transport.requests.first { $0.url?.path.contains("userDevice") == true }?.httpBody
        )
        let json = try #require(try JSONSerialization.jsonObject(with: body) as? [String: Any])

        #expect(json["fcmToken"] as? String == "ios-apns-pending")
        #expect(json["deviceId"] as? String == "DEVICE-1")
        #expect((json["fcmToken"] as? String)?.isEmpty == false)
    }

    // MARK: - 서버 DTO 와의 계약
    //
    // 서버(`monglife-discovery`)의 AppleJoinRequestDto / AppleLoginRequestDto 는
    // 필드를 @NotBlank 로 막아 두고 모르는 키는 무시한다.
    // 키가 하나라도 어긋나면 400 이 나므로 **정확한 키 집합**을 여기서 고정한다.

    @Test("가입 요청의 키 집합이 서버 DTO 와 정확히 일치한다")
    func joinRequestMatchesServerContract() async throws {
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [
            .status(404, #"{"code":"DISCOVERY-ACCOUNT-101","httpStatus":404}"#),
            .ok(loginOK),
        ])
        transport.stub("public/auth/join/apple", [.ok(#"{"result":{}}"#)])
        let (service, _) = makeService(transport: transport)

        _ = try await service.signIn()

        let body = try #require(
            transport.requests.first { $0.url?.path.contains("join/apple") == true }?.httpBody
        )
        let json = try #require(try JSONSerialization.jsonObject(with: body) as? [String: Any])

        #expect(Set(json.keys) == ["socialAccountId", "identityToken", "email", "name"])
    }

    @Test("로그인 요청의 키 집합이 서버 DTO 와 정확히 일치한다")
    func loginRequestMatchesServerContract() async throws {
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [.ok(loginOK)])
        let (service, _) = makeService(transport: transport)

        _ = try await service.signIn()

        let body = try #require(
            transport.requests.first { $0.url?.path.contains("login/apple") == true }?.httpBody
        )
        let json = try #require(try JSONSerialization.jsonObject(with: body) as? [String: Any])

        // ⚠️ email 이 없어야 한다. 서버는 검증된 토큰의 sub 로만 계정을 찾는다.
        #expect(Set(json.keys) == [
            "socialAccountId", "identityToken", "deviceId",
            "appPackageName", "deviceName", "buildVersion",
        ])
    }

    @Test("기기 등록 요청의 키 집합이 서버 DTO 와 정확히 일치한다")
    func deviceRequestMatchesServerContract() async throws {
        let transport = StubTransport()
        transport.stub("public/auth/login/apple", [.ok(loginOK)])
        let (service, _) = makeService(transport: transport)

        _ = try await service.signIn()

        let body = try #require(
            transport.requests.first { $0.url?.path.contains("userDevice") == true }?.httpBody
        )
        let json = try #require(try JSONSerialization.jsonObject(with: body) as? [String: Any])

        #expect(Set(json.keys) == ["deviceId", "deviceName", "appPackageName", "fcmToken"])
    }

    @Test("강제 업데이트 여부를 읽는다")
    func verifiesAppVersion() async throws {
        let transport = StubTransport()
        transport.stub("public/auth/verify/version", [
            .ok(#"{"result":{"appPackageName":"com.mongs.wear","buildVersion":"0.1.0","mustUpdate":true}}"#),
        ])
        let (service, _) = makeService(transport: transport)

        let check = try await service.verifyAppVersion()

        #expect(check.mustUpdate)
        let url = try #require(transport.requests.first?.url?.absoluteString)
        #expect(url.contains("appPackageName=com.mongs.wear"))
        #expect(url.contains("buildVersion=0.1.0"))
    }
}

import Foundation
import MongsModel

/// 인증 서비스
///
/// Android `application/auth-application` + `data/auth-data/.../AuthWebAdapter.kt` 를 합친 것.
///
/// 엔드포인트는 전부 **discovery** 서버이고 토큰을 붙이지 않는다.
public actor AuthService {

    /// Apple 계약 경로.
    /// Android 가 Google Credential 계약을 `public/auth/{join,login}/credential` 로 잡아 둔 것과
    /// 같은 규칙이다. 서버에 추가할 때 이 두 상수만 실제 경로에 맞추면 된다.
    static let joinPath = "public/auth/join/apple"
    static let loginPath = "public/auth/login/apple"

    private let api: APIClient
    private let tokenStore: TokenStore
    private let signInClient: any AppleSignInProviding
    private let identity: ClientIdentity

    public init(
        api: APIClient,
        tokenStore: TokenStore,
        signInClient: any AppleSignInProviding,
        identity: ClientIdentity
    ) {
        self.api = api
        self.tokenStore = tokenStore
        self.signInClient = signInClient
        self.identity = identity
    }

    /// 강제 업데이트 여부
    ///
    /// Android `LayoutViewModel` 이 가장 먼저 부르는 것. 서버가 `mustUpdate` 를 주면
    /// 앱은 업데이트 안내 화면에서 멈춘다.
    public func verifyAppVersion() async throws -> AppVersionCheck {
        try await api.request(Endpoint(
            host: .discovery,
            method: .get,
            path: "public/auth/verify/version",
            query: [
                "appPackageName": identity.appPackageName,
                "buildVersion": identity.buildVersion,
            ],
            requiresAuthorization: false
        ))
    }

    /// 로그인. 필요하면 가입 후 다시 로그인한다.
    ///
    /// Android `LoginViewModel` 이 하던 흐름 그대로다 — 서버가 미가입
    /// (`DISCOVERY-ACCOUNT-101`)을 알려주면 join 후 login 을 재시도한다.
    /// 원본은 `signOut()` 때문에 자격 증명을 다시 얻을 수 없어 `pendingGoogleAccount` 로
    /// 캐시해 뒀는데, 여기서는 자격 증명이 그대로 살아 있어 그럴 필요가 없다.
    /// 이미 받아 둔 Apple 자격으로 로그인한다.
    ///
    /// 화면의 `SignInWithAppleButton` 이 표시(present)까지 맡으므로, 그 결과를 여기로 넘긴다.
    @discardableResult
    public func signIn(credential: AppleCredential) async throws -> Session {
        // ⚠️ 로그인보다 먼저다. 등록되지 않은 deviceId 로 로그인하면 서버가
        // DISCOVERY-DEVICE-101 을 준다. Android LoginUseCase 도 같은 순서다.
        try await registerDevice()

        do {
            return try await login(credential: credential)
        } catch let error as APIError where error.isNeedJoin {
            try await join(credential: credential)
            return try await login(credential: credential)
        }
    }

    /// 로그인 시트를 직접 띄워 로그인한다.
    @discardableResult
    public func signIn() async throws -> Session {
        let credential = try await signInClient.signIn()

        // ⚠️ 로그인보다 먼저다. 등록되지 않은 deviceId 로 로그인하면 서버가
        // DISCOVERY-DEVICE-101 을 준다. Android LoginUseCase 도 같은 순서다.
        try await registerDevice()

        do {
            return try await login(credential: credential)
        } catch let error as APIError where error.isNeedJoin {
            try await join(credential: credential)
            return try await login(credential: credential)
        }
    }

    /// 기기 등록
    ///
    /// Android `LoginUseCase` 안에서 로그인 직전에 호출되는 것과 같다.
    public func registerDevice(pushToken: String? = nil) async throws {
        try await api.send(try Endpoint.json(
            host: .discovery,
            method: .post,
            path: "public/userDevice",
            body: CreateDeviceRequest(identity: identity, pushToken: pushToken),
            requiresAuthorization: false
        ))
    }

    #if DEBUG
    /// 개발용 로그인 (Debug 전용)
    ///
    /// 서버에 Apple 엔드포인트가 아직 없어서, 그때까지 **기존 Google/legacy 경로**로
    /// 진짜 세션을 받아 화면 작업을 이어가기 위한 것이다.
    /// 가짜 세션을 넣는 우회와 달리 **실제 서버·실제 토큰·실제 재발급 경로**를 그대로 탄다.
    ///
    /// Apple 엔드포인트가 나오면 이 메서드는 지운다.
    @discardableResult
    public func devSignIn(email: String, socialAccountId: String) async throws -> Session {
        try await registerDevice()

        struct LegacyLoginRequest: Encodable {
            let deviceId: String
            let email: String
            let socialAccountId: String
            let appPackageName: String
            let deviceName: String
            let buildVersion: String
        }

        let body = LegacyLoginRequest(
            deviceId: identity.deviceId,
            email: email,
            socialAccountId: socialAccountId,
            appPackageName: identity.appPackageName,
            deviceName: identity.deviceName,
            buildVersion: identity.buildVersion
        )

        let result: LoginResult = try await api.request(try Endpoint.json(
            host: .discovery,
            method: .post,
            path: "public/auth/login",
            body: body,
            requiresAuthorization: false
        ))

        return await tokenStore.save(Session(
            accountId: result.accountId,
            accessToken: result.accessToken,
            refreshToken: result.refreshToken,
            version: 0
        ))
    }
    #endif

    /// 저장된 세션이 있는지. 앱 시작 시 로그인 화면을 건너뛸지 판단한다.
    ///
    /// 토큰의 유효성까지 확인하지는 않는다 — 만료됐으면 첫 요청이 401 을 받고
    /// `APIClient` 가 알아서 재발급한다. 재발급까지 실패하면 그때 로그인 화면으로 간다.
    public func hasSession() async -> Bool {
        await tokenStore.session() != nil
    }

    /// 로그아웃. 서버 호출이 실패해도 로컬 세션은 반드시 지운다.
    public func signOut() async {
        if let session = await tokenStore.session() {
            let endpoint = try? Endpoint.json(
                host: .discovery,
                method: .post,
                path: "public/auth/logout",
                body: LogoutRequest(refreshToken: session.refreshToken),
                requiresAuthorization: false
            )
            if let endpoint { try? await api.send(endpoint) }
        }
        // 서버가 죽어 있어도 사용자는 로그아웃돼야 한다.
        await tokenStore.delete()
    }

    // MARK: - 내부

    private func join(credential: AppleCredential) async throws {
        try await api.send(try Endpoint.json(
            host: .discovery,
            method: .post,
            path: Self.joinPath,
            body: AppleJoinRequest(credential: credential),
            requiresAuthorization: false
        ))
    }

    private func login(credential: AppleCredential) async throws -> Session {
        let result: LoginResult = try await api.request(try Endpoint.json(
            host: .discovery,
            method: .post,
            path: Self.loginPath,
            body: AppleLoginRequest(credential: credential, identity: identity),
            requiresAuthorization: false
        ))

        return await tokenStore.save(Session(
            accountId: result.accountId,
            accessToken: result.accessToken,
            refreshToken: result.refreshToken,
            version: 0
        ))
    }
}

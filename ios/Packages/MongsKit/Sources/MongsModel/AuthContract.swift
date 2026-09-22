import Foundation

/// Sign in with Apple 로 받은 자격 증명
///
/// Android `core/auth-core/.../vo/GoogleAccountVo.kt` 자리를 대신한다.
/// 필드 대응: `googleAccountId` → `userIdentifier`, `idToken` → `identityToken`.
public struct AppleCredential: Sendable, Equatable {

    /// Apple 이 주는 안정적인 사용자 식별자. identity token 의 `sub` 와 같다.
    /// 서버의 `socialAccountId` 로 들어간다.
    public let userIdentifier: String

    /// Apple 이 서명한 JWT. 서버가 Apple JWKS 로 검증한다.
    public let identityToken: String

    /// ⚠️ **최초 인증 때만 값이 온다.** 재로그인 시엔 nil 이다.
    /// 서버가 첫 응답에서 저장해 두어야 한다.
    /// 사용자가 이메일 가리기를 택하면 `@privaterelay.appleid.com` 주소가 온다.
    public let email: String?

    /// 최초 인증 때만 온다.
    public let fullName: String?

    public init(userIdentifier: String, identityToken: String, email: String?, fullName: String?) {
        self.userIdentifier = userIdentifier
        self.identityToken = identityToken
        self.email = email
        self.fullName = fullName
    }
}

/// 앱 버전 검증 응답
///
/// Android `VerifyAppVersionResponseDto` 이식.
public struct AppVersionCheck: Decodable, Sendable, Equatable {
    public let appPackageName: String
    public let buildVersion: String
    public let mustUpdate: Bool
}

/// 로그인 응답
///
/// Android `LoginResponseDto` 이식.
public struct LoginResult: Decodable, Sendable, Equatable {
    public let accountId: Int64
    public let accessToken: String
    public let refreshToken: String
}

/// 앱/기기 정보 — 로그인 요청에 함께 실린다.
public struct ClientIdentity: Sendable, Equatable {
    public let deviceId: String
    public let appPackageName: String
    public let deviceName: String
    public let buildVersion: String

    public init(deviceId: String, appPackageName: String, deviceName: String, buildVersion: String) {
        self.deviceId = deviceId
        self.appPackageName = appPackageName
        self.deviceName = deviceName
        self.buildVersion = buildVersion
    }
}

// MARK: - 요청 본문

/// 회원 가입 요청 (Apple 계약)
///
/// Android `CredentialJoinRequestDto` 와 같은 모양이다. 서버는 `identityToken` 을
/// Apple JWKS 로 검증하고 `sub` 가 `socialAccountId` 와 일치하는지 확인하면 된다.
///
/// `email`/`name` 이 **optional** 인 것이 Google 계약과 다른 점이다 —
/// Apple 은 최초 인증 때만 이 값들을 준다.
public struct AppleJoinRequest: Encodable, Sendable {
    public let socialAccountId: String
    public let identityToken: String
    public let email: String?
    public let name: String?

    public init(credential: AppleCredential) {
        self.socialAccountId = credential.userIdentifier
        self.identityToken = credential.identityToken
        self.email = credential.email
        self.name = credential.fullName
    }
}

/// 로그인 요청 (Apple 계약)
///
/// Android `CredentialLoginRequestDto` 대응. `email` 이 없는 이유는 위와 같다 —
/// 서버는 `socialAccountId` 로 계정을 찾아야 한다.
public struct AppleLoginRequest: Encodable, Sendable {
    public let socialAccountId: String
    public let identityToken: String
    public let deviceId: String
    public let appPackageName: String
    public let deviceName: String
    public let buildVersion: String

    public init(credential: AppleCredential, identity: ClientIdentity) {
        self.socialAccountId = credential.userIdentifier
        self.identityToken = credential.identityToken
        self.deviceId = identity.deviceId
        self.appPackageName = identity.appPackageName
        self.deviceName = identity.deviceName
        self.buildVersion = identity.buildVersion
    }
}

/// 기기 등록 요청
///
/// Android `application/auth-application/.../port/web/request/CreateDeviceRequest` 이식.
///
/// ⚠️ **로그인보다 먼저 보내야 한다.** 서버는 등록되지 않은 `deviceId` 로 로그인하면
/// `DISCOVERY-DEVICE-101`(기기 정보가 존재하지 않습니다) 을 준다.
/// Android `LoginUseCase` 도 `createDevice` → `login` 순서다.
public struct CreateDeviceRequest: Encodable, Sendable {
    public let deviceId: String
    public let deviceName: String
    public let appPackageName: String
    /// ⚠️ 서버가 `@NotBlank` 로 막아 두어 빈 값을 보낼 수 없다.
    /// iOS 는 FCM 을 쓰지 않으므로(알림은 v1 범위 밖) 자리표시자를 보낸다.
    /// APNs 를 붙일 때 실제 토큰으로 바꾸고, 서버도 토큰 종류를 구분해야 한다.
    public let fcmToken: String

    public init(identity: ClientIdentity, pushToken: String? = nil) {
        self.deviceId = identity.deviceId
        self.deviceName = identity.deviceName
        self.appPackageName = identity.appPackageName
        self.fcmToken = pushToken ?? "ios-apns-pending"
    }
}

public struct LogoutRequest: Encodable, Sendable {
    public let refreshToken: String
    public init(refreshToken: String) { self.refreshToken = refreshToken }
}

/// 인증 오류
///
/// Android `core/auth-core/.../error/AuthCoreErrorCode.kt` 대응.
public enum AuthError: Error, Equatable, Sendable {
    /// 사용자가 로그인 시트를 닫았다. **오류 메시지를 띄우지 않는다** —
    /// 의도한 행동에 경고를 띄우면 안 된다. Android 도 `GOOGLE_LOGIN_CANCELED` 를 숨긴다.
    case canceled
    /// Apple 이 identity token 을 주지 않았다
    case missingIdentityToken
    /// 로그인 자체가 실패했다
    case failed(String)

    public var message: String {
        switch self {
        case .canceled: ""
        case .missingIdentityToken: "Apple 로그인 정보를 받지 못했습니다."
        case let .failed(reason): "로그인에 실패했습니다. (\(reason))"
        }
    }

    public var isMessageShown: Bool { self != .canceled }
}

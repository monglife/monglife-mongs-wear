import Foundation

/// 로그인 세션
///
/// Android `domain/auth-domain/.../model/Session.kt` +
/// `core/data-core/.../persistence/entity/SessionEntity.kt` 이식.
public struct Session: Sendable, Equatable, Codable {

    public let accountId: Int64
    public let accessToken: String
    public let refreshToken: String

    /// 재발급 세대 번호
    ///
    /// **단일 비행(single-flight) 재발급의 핵심이다.** 401 이 동시에 여러 개 터졌을 때,
    /// 각 요청은 자기가 들고 있던 세션의 version 과 저장소의 현재 version 을 비교해
    /// "이미 남이 갱신했다"를 판별한다. 그래야 refreshToken 을 여러 번 소모하지 않는다.
    /// Android `AuthorizationInterceptor` 가 같은 방식이다.
    public let version: Int64

    public init(accountId: Int64, accessToken: String, refreshToken: String, version: Int64 = 0) {
        self.accountId = accountId
        self.accessToken = accessToken
        self.refreshToken = refreshToken
        self.version = version
    }

    /// 재발급된 토큰으로 갱신하며 세대를 하나 올린다.
    public func reissued(accessToken: String, refreshToken: String) -> Session {
        Session(
            accountId: accountId,
            accessToken: accessToken,
            refreshToken: refreshToken,
            version: version + 1
        )
    }
}

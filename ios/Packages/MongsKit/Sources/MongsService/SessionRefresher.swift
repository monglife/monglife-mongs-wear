import Foundation
import MongsModel

/// 토큰 재발급 조정자
///
/// Android `core/data-core/.../web/interceptor/AuthorizationInterceptor.kt` 의
/// 재발급 알고리즘을 그대로 옮긴다. OkHttp 인터셉터가 동기라 원본은
/// `runBlocking { mutex.withLock { ... } }` 였지만, 여기서는 actor 가 직렬화를 맡는다.
///
/// 알고리즘의 핵심은 **세대(version) 비교**다:
/// 401 이 동시에 여러 개 터졌을 때, 각 요청은 자기가 요청에 실어 보냈던 세션의 version 과
/// 저장소의 현재 version 을 비교한다. 저장소 쪽이 더 크면 다른 요청이 이미 재발급을
/// 끝냈다는 뜻이므로, refreshToken 을 다시 쓰지 않고 새 토큰만 받아 간다.
/// 이 비교가 없으면 refreshToken 이 여러 번 소모되어 서버가 세션을 끊는다.
public actor SessionRefresher {

    /// 재발급 요청을 실제로 보내는 부분. `APIClient` 가 주입한다
    /// (`APIClient` → `SessionRefresher` → `APIClient` 순환을 끊기 위해서다).
    public typealias Reissue = @Sendable (Session) async -> (accessToken: String, refreshToken: String)?

    private let tokenStore: TokenStore
    private let reissue: Reissue
    private var inFlight: Task<Session?, Never>?

    public init(tokenStore: TokenStore, reissue: @escaping Reissue) {
        self.tokenStore = tokenStore
        self.reissue = reissue
    }

    /// 401 을 받은 요청이 부른다.
    ///
    /// - Parameter staleSession: 그 요청이 실어 보냈던 세션
    /// - Returns: 재시도에 쓸 새 세션. `nil` 이면 세션이 끝난 것이다 (403 으로 취급).
    public func refresh(staleSession: Session) async -> Session? {
        let current = await tokenStore.session()

        // 저장소에 세션이 없다 = 그 사이 로그아웃됐거나 재발급이 실패했다.
        guard let current else { return nil }

        // 남이 이미 갱신했다. refreshToken 을 또 쓰지 않고 새 토큰만 받아 간다.
        if staleSession.version < current.version { return current }

        // 이미 도는 재발급이 있으면 그 결과를 함께 기다린다.
        if let inFlight { return await inFlight.value }

        let task = Task<Session?, Never> { [tokenStore, reissue] in
            guard let tokens = await reissue(current) else {
                // 재발급 실패 = 세션 종료. Android 도 여기서 세션을 지운다.
                await tokenStore.delete()
                return nil
            }
            let next = current.reissued(
                accessToken: tokens.accessToken,
                refreshToken: tokens.refreshToken
            )
            return await tokenStore.save(next)
        }

        inFlight = task
        let result = await task.value
        inFlight = nil
        return result
    }
}

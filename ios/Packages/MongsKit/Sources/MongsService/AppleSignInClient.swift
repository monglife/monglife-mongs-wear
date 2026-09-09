import AuthenticationServices
import Foundation
import MongsModel

/// Apple 로그인 클라이언트
///
/// Android `core/auth-core/.../client/GoogleAuthClient.kt` 자리를 대신한다.
/// 프로토콜로 두는 이유는 테스트 때문이다 — `ASAuthorizationController` 는
/// 시스템 UI 를 띄우므로 단위 테스트에서 스텁으로 갈아끼운다.
public protocol AppleSignInProviding: Sendable {
    func signIn() async throws -> AppleCredential
}

extension AppleCredential {

    /// `ASAuthorization` 을 도메인 값으로 옮긴다.
    ///
    /// 두 경로가 공유한다 — 화면의 `SignInWithAppleButton` 이 주는 결과와
    /// `AppleSignInClient` 가 직접 띄운 컨트롤러의 결과.
    public init(authorization: ASAuthorization) throws {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential else {
            throw AuthError.failed("예상하지 못한 자격 증명 형식")
        }
        guard
            let tokenData = credential.identityToken,
            let identityToken = String(data: tokenData, encoding: .utf8)
        else {
            throw AuthError.missingIdentityToken
        }

        // ⚠️ email / fullName 은 **최초 인증 때만** 온다. 재로그인 시엔 nil 이다.
        let fullName = credential.fullName.flatMap { name -> String? in
            let formatted = PersonNameComponentsFormatter().string(from: name)
            return formatted.isEmpty ? nil : formatted
        }

        self.init(
            userIdentifier: credential.user,
            identityToken: identityToken,
            email: credential.email,
            fullName: fullName
        )
    }

    /// 사용자가 시트를 닫은 것인지 판별한다. 취소는 오류로 다루지 않는다.
    public static func mapSignInFailure(_ error: any Error) -> AuthError {
        if let authError = error as? ASAuthorizationError, authError.code == .canceled {
            return .canceled
        }
        return .failed(error.localizedDescription)
    }
}

/// `ASAuthorizationController` 구현 (watchOS 6+ 네이티브)
///
/// 화면에서는 보통 `SignInWithAppleButton` 을 쓴다 — 시스템 버튼 사용이 App Store 심사 요건이고,
/// 버튼이 표시(present)까지 맡아 준다. 이 타입은 버튼 없이 로그인을 다시 요구해야 할 때를 위해 남긴다.
///
/// Android 의 Google 로그인은 `ActivityResultRegistry` 왕복 + `signOut()` 선행 호출 +
/// GMS 상태 코드 해석이 필요했지만, Apple 로그인은 그 전부가 없다.
public final class AppleSignInClient: NSObject, AppleSignInProviding, @unchecked Sendable {

    /// 진행 중인 요청. `ASAuthorizationController` 는 델리게이트로 결과를 주므로
    /// 콜백을 continuation 에 이어 붙이고, 컨트롤러가 살아 있도록 붙들어 둔다.
    private var continuation: CheckedContinuation<AppleCredential, any Error>?
    private var controller: ASAuthorizationController?

    public override init() { super.init() }

    public func signIn() async throws -> AppleCredential {
        try await withCheckedThrowingContinuation { continuation in
            let request = ASAuthorizationAppleIDProvider().createRequest()
            // 최초 인증 때만 값이 오지만, 그때 받으려면 요청해 두어야 한다.
            request.requestedScopes = [.fullName, .email]

            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self

            self.continuation = continuation
            self.controller = controller

            controller.performRequests()
        }
    }

    private func finish(_ result: Result<AppleCredential, any Error>) {
        // 컨트롤러가 델리게이트를 두 번 부르는 경우를 대비해 continuation 을 먼저 비운다.
        // 두 번 resume 하면 크래시다.
        guard let continuation else { return }
        self.continuation = nil
        self.controller = nil
        continuation.resume(with: result)
    }
}

extension AppleSignInClient: ASAuthorizationControllerDelegate {

    public func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        finish(Result { try AppleCredential(authorization: authorization) })
    }

    public func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: any Error
    ) {
        finish(.failure(AppleCredential.mapSignInFailure(error)))
    }
}

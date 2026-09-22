import Foundation
import MongsModel
import MongsService
import Observation

/// 앱 진입 게이트
///
/// Android `presentation/viewmodel-presentation/.../layout/LayoutViewModel.kt` +
/// `wear-view-presentation/.../layout/LayoutView.kt` 의 게이트 로직 이식.
///
/// 순서가 정해져 있다: 로딩 → 강제 업데이트 → 로그인 → 메인.
/// 버전 체크를 로그인보다 먼저 하는 이유는, 서버 계약이 바뀐 구버전 앱이
/// 로그인부터 실패하면 사용자가 원인을 알 수 없기 때문이다.
@Observable
@MainActor
public final class RootViewModel: ErrorReportingViewModel {

    public enum Phase: Equatable, Sendable {
        case loading
        case mustUpdate
        case signedOut
        case signedIn
        /// 서버에 닿지 못했다. 재시도 버튼을 준다.
        case unreachable
    }

    public private(set) var phase: Phase = .loading
    public private(set) var isSigningIn = false

    private let authService: AuthService

    public init(authService: AuthService) {
        self.authService = authService
    }

    #if DEBUG
    /// 개발용 로그인 자격 (Debug 전용)
    ///
    /// 서버에 Apple 엔드포인트가 아직 없어서, 그때까지 기존 legacy 로그인으로
    /// **진짜 세션**을 받아 화면 작업을 이어간다. 가짜 세션을 넣지 않기 때문에
    /// gateway 호출·토큰 재발급까지 실제 경로를 그대로 탄다.
    ///
    /// 실행 인자로 준다. 다시 빌드할 필요가 없다:
    /// ```
    /// xcrun simctl launch <UDID> com.monglife.mongs.wear.ios \
    ///   -MongsDevLoginEmail ios-dev@monglife.test \
    ///   -MongsDevLoginSocialId ios-dev-000001
    /// ```
    /// Xcode 에서는 스킴 편집 → Run → Arguments Passed On Launch 에 같은 값을 넣는다.
    /// 로그인 중 화면을 눈으로 확인하기 위한 것 (Debug 전용).
    ///
    /// 실제 Apple 로그인은 시계에 암호가 설정돼 있어야 하고 서버 엔드포인트도 필요해서,
    /// 로딩 상태를 보려면 그 전부가 갖춰져야 한다. 화면만 확인할 때 쓴다.
    ///
    /// ```
    /// xcrun simctl launch <UDID> com.monglife.mongs.wear.ios -MongsPreviewSigningIn YES
    /// ```
    static var isSigningInPreview: Bool {
        UserDefaults.standard.bool(forKey: "MongsPreviewSigningIn")
    }

    static var devLoginCredential: (email: String, socialAccountId: String)? {
        let defaults = UserDefaults.standard
        guard
            let email = defaults.string(forKey: "MongsDevLoginEmail"), !email.isEmpty,
            let socialId = defaults.string(forKey: "MongsDevLoginSocialId"), !socialId.isEmpty
        else { return nil }
        return (email, socialId)
    }
    #endif

    /// 앱 시작 시 한 번.
    public func start() async {
        phase = .loading

        #if DEBUG
        // 로그인 중 화면에서 멈춘다. 서버도 Apple 로그인도 타지 않는다.
        if Self.isSigningInPreview {
            phase = .signedOut
            isSigningIn = true
            return
        }
        #endif

        do {
            let check = try await authService.verifyAppVersion()
            guard !check.mustUpdate else {
                phase = .mustUpdate
                return
            }
        } catch {
            // 버전 확인에 실패하면 로그인 화면으로 밀어붙이지 않는다.
            // 서버가 죽었는데 로그인 화면을 보여주면 사용자가 자기 계정 문제로 오해한다.
            phase = .unreachable
            return
        }

        if await authService.hasSession() {
            phase = .signedIn
            return
        }

        #if DEBUG
        // 개발용 자격이 주어졌으면 그걸로 로그인한다.
        if let credential = Self.devLoginCredential {
            do {
                _ = try await authService.devSignIn(
                    email: credential.email,
                    socialAccountId: credential.socialAccountId
                )
                phase = .signedIn
                return
            } catch {
                await ErrorBanner.shared.post(error: error)
            }
        }
        #endif

        phase = .signedOut
    }

    /// 화면의 `SignInWithAppleButton` 결과로 로그인한다.
    public func signIn(credential: AppleCredential) async {
        await performSignIn { try await self.authService.signIn(credential: credential) }
    }

    /// 로그인 시트를 직접 띄워 로그인한다.
    public func signIn() async {
        await performSignIn { try await self.authService.signIn() }
    }

    /// 사용자가 로그인 시트를 닫았거나 실패했을 때.
    public func signInFailed(_ error: any Error) async {
        isSigningIn = false
        guard (error as? AuthError) != .canceled else { return }
        await ErrorBanner.shared.post(error: error)
    }

    private func performSignIn(_ body: @escaping () async throws -> Session) async {
        guard !isSigningIn else { return }
        isSigningIn = true
        defer { isSigningIn = false }

        do {
            _ = try await body()
            phase = .signedIn
        } catch let error as AuthError where error == .canceled {
            // 사용자가 닫은 것뿐이다. 아무 일도 일어나지 않은 것처럼 둔다.
        } catch {
            await ErrorBanner.shared.post(error: error)
        }
    }

    public func signOut() async {
        await authService.signOut()
        phase = .signedOut
    }
}

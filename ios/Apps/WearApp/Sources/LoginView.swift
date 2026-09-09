import AuthenticationServices
import MongsModel
import MongsService
import MongsViewModel
import SwiftUI

/// 로그인 화면
///
/// Android `wear-view-presentation/.../layout/LoginContent.kt` 이식.
/// 원본 구성 그대로다 — 위 60% 에 로고(아래 정렬), 아래 40% 에 버튼(위 정렬).
/// 로그인이 진행되면 로고가 닫힌 것으로 바뀌고 버튼 자리에 로딩 스피너가 들어간다.
///
/// **버튼만 원본과 다르다.** Android 는 구글 로그인 이미지를 직접 그리지만,
/// Apple 은 시스템 버튼 사용이 App Store 심사 요건이라 `SignInWithAppleButton` 을 쓴다.
struct LoginView: View {

    let viewModel: RootViewModel

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        // 배경은 RootView 가 깔아 둔다 (Android LayoutView 와 같은 구조).
        GeometryReader { geometry in
            // Compose weight 와 같게 — 간격(5)을 뺀 남은 공간에 비율을 건다.
            let available = geometry.size.height - 5

                VStack(spacing: 5) {
                    // 위 60% — 로고를 아래에 붙인다
                    VStack {
                        Spacer(minLength: 0)
                        // 로그인 중에 알이 열린다 (원본 `Logo(isOpen = !signInButton)`).
                        MongsLogo(isOpen: viewModel.isSigningIn)
                    }
                    .frame(height: available * 0.6)

                    // 아래 40% — 버튼은 위에, 로딩은 가운데
                    VStack {
                        if viewModel.isSigningIn {
                            Spacer(minLength: 0)
                            LoadingBar()
                            Spacer(minLength: 0)
                        } else {
                            // 시스템 버튼이 로그인 시트 표시까지 맡는다.
                            // 처음엔 버튼을 비활성화하고 투명 버튼을 얹어 ViewModel 로 돌렸는데,
                            // 그 방식은 **탭이 아예 닿지 않았다** — 빈 라벨 Button 은 크기가 없어서
                            // contentShape 를 줘도 잡히는 영역이 사실상 점 하나였다.
                            SignInWithAppleButton(.signIn) { request in
                                // ⚠️ 최초 인증 때만 오는 값들이라, 그때 받으려면 요청해 둬야 한다.
                                request.requestedScopes = [.fullName, .email]
                            } onCompletion: { result in
                                Task {
                                    switch result {
                                    case let .success(authorization):
                                        do {
                                            let credential = try AppleCredential(authorization: authorization)
                                            await viewModel.signIn(credential: credential)
                                        } catch {
                                            await viewModel.signInFailed(error)
                                        }
                                    case let .failure(error):
                                        await viewModel.signInFailed(AppleCredential.mapSignInFailure(error))
                                    }
                                }
                            }
                            .signInWithAppleButtonStyle(.white)
                            .frame(width: 160, height: 45)
                            Spacer(minLength: 0)
                        }
                    }
                    .frame(height: available * 0.4)
            }
            .frame(maxWidth: .infinity)
        }
        .task {
            await loader.preload(["icon_logo_open", "icon_logo_not_open", "icon_loading"])
        }
    }
}

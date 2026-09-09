import MongsService
import SwiftUI

/// 전역 오류 배너
///
/// Android `LayoutView.kt` 가 `BaseViewModel.errorEvent` 를 모아 `Toast` 로 띄우는 자리다.
/// **watchOS 에는 Toast 가 없어서** 화면 위에 얹는 배너로 만든다.
///
/// 구독은 앱 전체에서 한 곳(루트)뿐이다. 화면마다 구독하면 같은 메시지가 여러 번 뜬다.
struct ErrorBannerOverlay: ViewModifier {

    @State private var message: String?

    func body(content: Content) -> some View {
        content
            .overlay(alignment: .top) {
                if let message {
                    Text(message)
                        .font(.system(size: 12))
                        .foregroundStyle(.white)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .fill(.red.opacity(0.85))
                        )
                        .padding(.horizontal, 8)
                        .transition(.move(edge: .top).combined(with: .opacity))
                }
            }
            .animation(.easeInOut(duration: 0.2), value: message)
            .task {
                for await next in await ErrorBanner.shared.messages() {
                    message = next
                    // 배너가 읽을 시간을 준 뒤 스스로 사라진다.
                    try? await Task.sleep(for: .seconds(2.5))
                    message = nil
                }
            }
    }
}

extension View {
    func errorBanner() -> some View { modifier(ErrorBannerOverlay()) }
}

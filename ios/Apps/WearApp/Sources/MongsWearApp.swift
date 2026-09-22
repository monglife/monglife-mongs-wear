import SwiftUI
import WatchKit

/// Android `app/wear-app/.../activity/MainApplication.kt` + `MainActivity.kt` 대응.
///
/// 원본이 239 LOC 뿐인 것처럼 이 앱 타겟도 얇게 유지한다. 로직은 전부 `MongsKit` 에 있다.
@main
struct MongsWearApp: App {

    @State private var container = AppContainer()
    @State private var spriteLoader = SpriteLoader()

    /// APNs 등록 콜백은 SwiftUI 로 받을 수 없어 델리게이트가 필요하다.
    @WKApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(container)
                .environment(spriteLoader)
                // 오류 배너는 앱 전체에서 여기 한 곳만 구독한다.
                .errorBanner()
                // 델리게이트는 SwiftUI 가 만들기 때문에 주입이 안 된다. 여기서 채워 준다.
                .task { appDelegate.pushService = container.pushService }
        }
    }
}

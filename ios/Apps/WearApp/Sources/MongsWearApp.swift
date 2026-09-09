import SwiftUI

/// Android `app/wear-app/.../activity/MainApplication.kt` + `MainActivity.kt` 대응.
///
/// 원본이 239 LOC 뿐인 것처럼 이 앱 타겟도 얇게 유지한다. 로직은 전부 `MongsKit` 에 있다.
@main
struct MongsWearApp: App {

    @State private var container = AppContainer()
    @State private var spriteLoader = SpriteLoader()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(container)
                .environment(spriteLoader)
                // 오류 배너는 앱 전체에서 여기 한 곳만 구독한다.
                .errorBanner()
        }
    }
}

import SwiftUI

/// 설정 쪽
///
/// Android `.../pages/main/ConfigureContent.kt` 이식.
/// 3줄(1개 / 2개 / 2개)이고, 아래 두 줄에 음수 오프셋이 걸려 있어 원형으로 모인다.
///
/// 이동할 화면들이 아직 없어서 지금은 눌러도 아무 일도 하지 않는다.
struct ConfigureContentView: View {

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        VStack(spacing: 0) {
            MongsCircleTextButton(title: "i", borderName: "btn_border_purple_dark") {}

            HStack(spacing: 48) {
                MongsCircleButton(iconName: "btn_icon_charge", borderName: "btn_border_purple_dark") {}
                MongsCircleButton(iconName: "btn_icon_notice", borderName: "btn_border_purple_dark") {}
            }
            .offset(y: -14)

            HStack(spacing: 10) {
                MongsCircleButton(iconName: "btn_icon_feedback", borderName: "btn_border_purple_dark") {}
                MongsCircleButton(iconName: "btn_icon_setting", borderName: "btn_border_purple_dark") {}
            }
            .offset(y: -8)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task {
            await loader.preload([
                "btn_bg_circle",
                "btn_icon_charge", "btn_icon_notice", "btn_icon_feedback",
                "btn_icon_setting", "btn_border_purple_dark",
            ])
        }
    }
}

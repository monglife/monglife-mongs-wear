import SwiftUI

/// 설정 쪽
///
/// Android `.../pages/main/ConfigureContent.kt` 이식.
/// 3줄(1개 / 2개 / 2개)이고, 아래 두 줄에 음수 오프셋이 걸려 있어 원형으로 모인다.
///
/// 설정만 실제 화면으로 간다. 도움말·충전·공지·오류 신고는 v1 범위 밖이라
/// `NotReadyView` 자리표시자로 보낸다.
struct ConfigureContentView: View {

    /// 설정 화면 열기
    let onOpenSetting: () -> Void
    /// 아직 이식하지 않은 화면 열기 (자리표시자)
    let onNotReady: (NotReadyDestination) -> Void

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        VStack(spacing: 0) {
            MongsCircleTextButton(title: "i", borderName: "btn_border_purple_dark") { onNotReady(.help) }

            HStack(spacing: 48.ms) {
                MongsCircleButton(iconName: "btn_icon_charge", borderName: "btn_border_purple_dark") { onNotReady(.charge) }
                MongsCircleButton(iconName: "btn_icon_notice", borderName: "btn_border_purple_dark") { onNotReady(.notice) }
            }
            .offset(y: -14.ms)

            HStack(spacing: 10.ms) {
                MongsCircleButton(iconName: "btn_icon_feedback", borderName: "btn_border_purple_dark") { onNotReady(.feedback) }
                MongsCircleButton(iconName: "btn_icon_setting", borderName: "btn_border_purple_dark",
                                  action: onOpenSetting)
            }
            .offset(y: -8.ms)
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

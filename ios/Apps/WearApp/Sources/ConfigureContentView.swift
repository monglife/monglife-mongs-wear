import SwiftUI

/// 설정 쪽
///
/// Android `.../pages/main/ConfigureContent.kt` 이식.
/// 3줄(1개 / 2개 / 2개)이고, 아래 두 줄에 음수 오프셋이 걸려 있어 원형으로 모인다.
///
/// 도움말만 아직 화면이 없어 `NotReadyView` 자리표시자로 보낸다.
struct ConfigureContentView: View {

    /// 설정 화면 열기
    let onOpenSetting: () -> Void
    /// 충전 화면 열기
    let onOpenCharge: () -> Void
    /// 공지사항 열기
    let onOpenNotice: () -> Void
    /// 오류 신고 열기
    let onOpenFeedback: () -> Void
    /// 아직 이식하지 않은 화면 열기 (자리표시자)
    let onNotReady: (NotReadyDestination) -> Void

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        VStack(spacing: 0) {
            MongsCircleTextButton(title: "i", borderName: "btn_border_purple_dark") { onNotReady(.help) }

            HStack(spacing: 48.ms) {
                MongsCircleButton(iconName: "btn_icon_charge", borderName: "btn_border_purple_dark",
                                  action: onOpenCharge)
                MongsCircleButton(iconName: "btn_icon_notice", borderName: "btn_border_purple_dark",
                                  action: onOpenNotice)
            }
            .offset(y: -14.ms)

            HStack(spacing: 10.ms) {
                MongsCircleButton(iconName: "btn_icon_feedback", borderName: "btn_border_purple_dark",
                                  action: onOpenFeedback)
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

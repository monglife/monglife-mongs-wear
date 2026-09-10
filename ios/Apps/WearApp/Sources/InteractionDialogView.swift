import MongsModel
import MongsViewModel
import SwiftUI

/// 상호작용 다이얼로그
///
/// Android `dialog/pages/main/InteractionDialog.kt` 이식.
/// 몽을 탭하면 화면 전체를 85% 검은 막으로 덮고 그 위에 버튼을 띄운다.
/// **막 아무 곳이나 누르면 닫힌다** — 원본도 배경 Box 전체에 닫기 클릭이 걸려 있다.
///
/// 세로 비중 0.4 / 0.3 / 0.3 — 페이포인트, 재우기·쓰다듬기·똥치우기, 먹이·인벤토리.
struct InteractionDialogView: View {

    let mong: Mong
    let viewModel: MainSlotViewModel
    /// 먹이 메뉴 열기
    let onOpenFeed: () -> Void
    /// 인벤토리 열기
    let onOpenInventory: () -> Void

    @Environment(SpriteLoader.self) private var loader

    /// 알(level 0)은 아무것도 할 수 없다. 부화해야 상호작용이 열린다.
    private var isHatched: Bool { mong.level > 0 }
    /// 정상 상태에서만 되는 것들 (재우기·쓰다듬기·똥치우기)
    private var isNormal: Bool { mong.stateCode == .normal }

    /// 버튼 줄
    ///
    /// ⚠️ 원본 `Row` 는 `verticalAlignment` 를 주지 않아 기본값 **Top** 이다 —
    /// 버튼이 자기 밴드의 위쪽에 붙는다. SwiftUI `HStack` 은 세로 중앙이 기본이라
    /// 그대로 두면 줄 간격이 원본보다 벌어진다.
    @ViewBuilder
    private func buttonRow(height: CGFloat, @ViewBuilder content: () -> some View) -> some View {
        VStack(spacing: 0) {
            HStack(spacing: 8.ms) { content() }
            Spacer(minLength: 0)
        }
        .frame(height: height)
    }

    var body: some View {
        ZStack {
            Color.black.opacity(0.85)
                .ignoresSafeArea()
                .onTapGesture { viewModel.closeInteractionDialog() }

            GeometryReader { geometry in
                // Compose weight 는 고정 크기(끝의 Spacer 20dp)를 뺀 남은 공간에 비율을 건다.
                let available = geometry.size.height - 20

                VStack(spacing: 0) {
                    // 0.4 — 페이포인트. 원본만 `verticalAlignment = CenterVertically` 라 세로 중앙이다.
                    ZStack {
                        PayPointBox(payPoint: mong.payPoint, width: 100)
                    }
                    .frame(height: available * 0.4)

                    // 0.3 — 재우기 / 쓰다듬기 / 똥치우기
                    buttonRow(height: available * 0.3) {
                        MongsCircleButton(
                            iconName: "btn_icon_sleep", borderName: "btn_border_blue",
                            iconSize: 34, isEnabled: isHatched && isNormal
                        ) { Task { await viewModel.toggleSleep() } }

                        MongsCircleButton(
                            iconName: "btn_icon_stroke", borderName: "btn_border_pink",
                            iconSize: 34, isEnabled: isHatched && !mong.isSleep && isNormal
                        ) { Task { await viewModel.stroke() } }

                        MongsCircleButton(
                            iconName: "btn_icon_poop_clean", borderName: "btn_border_purple",
                            iconSize: 34, isEnabled: isHatched && !mong.isSleep && isNormal
                        ) { Task { await viewModel.cleanPoop() } }
                    }

                    // 0.3 — 먹이주기 / 인벤토리
                    buttonRow(height: available * 0.3) {
                        MongsCircleButton(
                            iconName: "btn_icon_feed", borderName: "btn_border_yellow",
                            iconSize: 34, isEnabled: isHatched && !mong.isSleep && mong.isInteractable,
                            action: onOpenFeed
                        )

                        MongsCircleButton(
                            iconName: "btn_icon_inventory", borderName: "btn_border_green",
                            iconSize: 34, isEnabled: isHatched && !mong.isSleep && mong.isInteractable,
                            action: onOpenInventory
                        )
                    }

                    Spacer().frame(height: 20.ms)
                }
                .frame(maxWidth: .infinity)
            }
        }
        .task {
            await loader.preload([
                "btn_bg_circle",
                "btn_icon_sleep", "btn_icon_stroke", "btn_icon_poop_clean",
                "btn_icon_feed", "btn_icon_inventory",
                "btn_border_blue", "btn_border_pink", "btn_border_purple",
                "btn_border_yellow", "btn_border_green",
                "point_bg", "point_icon_pay",
            ])
        }
    }
}

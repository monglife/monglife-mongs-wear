import MongsModel
import MongsViewModel
import SwiftUI

/// 랜덤 뽑기
///
/// Android `pages/randomDraw/RandomDrawView.kt` 이식.
/// 뽑기 기계가 화면 가운데 있고, 뽑는 동안 좌우로 흔들린다.
struct RandomDrawView: View {

    /// 기계가 기우는 최대 각도. Android `DRAW_MACHINE_MAX_DEGREE`.
    private static let maxDegree: Double = 7

    @State private var viewModel: RandomDrawViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader
    @State private var isShaking = false
    /// 흔들림의 방향. `repeatForever` 가 이 값의 변화를 타고 왕복한다.
    @State private var shakeToggle = false

    init(viewModel: RandomDrawViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            // 원본은 기본 배경 대신 걷기 배경(`bg_walking_gif`)을 쓴다.
            AnimatedSprite(sprite: loader.sprite(named: "bg_walking_gif"), contentMode: .fill)
                .ignoresSafeArea()

            if viewModel.phase == .loading {
                LoadingBar()
            } else {
                machine

                switch viewModel.phase {
                case .entering:
                    enteringDialog
                case .confirm:
                    ConfirmDialogView(
                        message: "뽑기를\n하시겠습니까?",
                        onConfirm: { Task { await viewModel.draw() } },
                        onCancel: viewModel.cancelConfirm
                    )
                case .result:
                    if let result = viewModel.result {
                        resultDialog(result)
                    }
                default:
                    EmptyView()
                }
            }
        }
        .task {
            await loader.preload(["bg_walking_gif", "btn_icon_luck_draw", "point_icon_pay", "icon_ticket"])
            await viewModel.load()
        }
        .onChange(of: viewModel.phase) { _, phase in
            isShaking = phase == .drawing
            // 애니메이션은 값이 바뀌어야 시작한다. 켜는 순간 한 번 뒤집어 준다.
            if isShaking { shakeToggle.toggle() }
        }
        .onChange(of: viewModel.shouldClose) { _, close in
            if close { onClose() }
        }
    }

    /// 뽑기 기계.
    ///
    /// 대기 중에는 **똑바로 서 있고**, 뽑는 동안만 좌우로 흔들린다.
    /// 원본도 `rotation` 초기값이 0 이고 `drawLoading` 일 때만 애니메이션을 건다 —
    /// 흔들림 각도를 그냥 토글하면 가만히 있을 때도 기울어 보인다.
    private var machine: some View {
        AnimatedSprite(sprite: loader.sprite(named: "btn_icon_luck_draw"), contentMode: nil)
            .frame(width: 90.ms, height: 155.ms)
            .rotationEffect(.degrees(angle))
            .animation(
                isShaking
                    ? .easeInOut(duration: 0.5).repeatForever(autoreverses: true)
                    : .easeOut(duration: 0.25),
                value: angle
            )
    }

    private var angle: Double {
        guard isShaking else { return 0 }
        return shakeToggle ? Self.maxDegree : -Self.maxDegree
    }

    /// 안내 다이얼로그
    ///
    /// Android `RandomDrawEnteringDialog.kt` — 소모될 티켓 1장과 페이포인트를 함께 보여준다.
    /// **둘 중 하나만 있으면 뽑을 수 있다.**
    private var enteringDialog: some View {
        ZStack {
            Color.black.opacity(0.9).ignoresSafeArea()

            VStack(spacing: 10.ms) {
                Text("랜덤뽑기")
                    .mongsFont(24)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)

                costRow(icon: "btn_icon_luck_draw", size: 24, text: "- 1")
                costRow(icon: "point_icon_pay", size: 20, text: "- \(RandomDrawViewModel.drawPayPoint)")

                MongsButton(
                    title: "뽑기", style: .blue,
                    isEnabled: viewModel.canDraw,
                    action: viewModel.askConfirm
                )
            }
            .padding(.vertical, 15.ms)
        }
    }

    private func costRow(icon: String, size: CGFloat, text: String) -> some View {
        HStack(spacing: 10.ms) {
            AnimatedSprite(sprite: loader.sprite(named: icon))
                .frame(width: size.ms, height: size.ms)
            Text(text)
                .mongsFont(16)
                .foregroundStyle(MongsColor.white)
                .lineLimit(1)
        }
    }

    /// 결과 다이얼로그
    ///
    /// Android `RandomDrawOverDialog.kt` — 뽑힌 것의 스프라이트와 이름을 띄운다.
    private func resultDialog(_ result: RandomDrawResult) -> some View {
        ZStack {
            Color.black.opacity(0.9).ignoresSafeArea()

            VStack(spacing: 10.ms) {
                if let sprite = Self.spriteName(for: result) {
                    AnimatedSprite(sprite: loader.sprite(named: sprite))
                        .frame(width: 60.ms, height: 60.ms)
                } else {
                    Text("?")
                        .mongsFont(24)
                        .foregroundStyle(MongsColor.white)
                }

                Text(result.randomDrawName)
                    .mongsFont(16)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)

                MongsButton(title: "닫기", style: .blue, width: 80, action: viewModel.closeResult)
            }
            .padding(.vertical, 15.ms)
        }
        .task {
            if let sprite = Self.spriteName(for: result) {
                await loader.preload([sprite])
            }
        }
    }

    /// 뽑힌 것의 종류로 스프라이트를 고른다. 먹이/간식은 있고 맵은 아직 없다.
    private static func spriteName(for result: RandomDrawResult) -> String? {
        switch result.inventoryTypeCode {
        case .food: FeedResourceCode.pngName(kind: .food, code: result.randomDrawCode)
        case .snack: FeedResourceCode.pngName(kind: .snack, code: result.randomDrawCode)
        case .map: nil
        }
    }
}

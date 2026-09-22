import MongsModel
import MongsViewModel
import SwiftUI

/// 환전 메뉴
///
/// Android `pages/exchange/ExchangeMenuView.kt` 이식.
/// 0.49 / 0.02 / 0.49 — 가운데 2dp 흰 줄이 두 칸을 가른다. `FeedMenuView` 와 같은 모양이다.
///
/// 위 칸의 문구가 "별가루 환전" 이 아니라 그냥 **"환전"** 이다 (원본 그대로).
/// 아래는 "걸음 수 환전".
struct ExchangeMenuView: View {

    let onSelect: (ExchangeViewModel.Kind) -> Void
    let onClose: () -> Void

    var body: some View {
        ZStack {
            DefaultBackground()

            GeometryReader { geometry in
                let available = geometry.size.height

                VStack(spacing: 0) {
                    entry("환전") { onSelect(.starPoint) }
                        .frame(height: available * 0.49)

                    ZStack {
                        Rectangle()
                            .fill(Color.white)
                            .frame(height: 2.ms)
                    }
                    .frame(height: available * 0.02)

                    entry("걸음 수 환전") { onSelect(.step) }
                        .frame(height: available * 0.49)
                }
            }
        }
    }

    private func entry(_ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .mongsFont(18)
                .foregroundStyle(MongsColor.white)
                .lineLimit(1)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                // 빈 영역도 탭이 먹어야 한다 — 원본은 Box 전체에 clickable 이 걸려 있다.
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

/// 환전 화면
///
/// Android `pages/exchange/ExchangeStepView.kt` + `ExchangeStarPointView.kt` 이식.
/// 세로 비중 0.2(페이포인트) / 0.5(단위 선택) / 0.3(환전 버튼).
struct ExchangeView: View {

    @State private var viewModel: ExchangeViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: ExchangeViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            DefaultBackground()

            if viewModel.isLoading {
                LoadingBar()
            } else {
                content
                if viewModel.isConfirming {
                    ConfirmDialogView(
                        message: "$\(viewModel.payPoint)\n환전하시겠습니까?",
                        onConfirm: { Task { await viewModel.exchange() } },
                        onCancel: viewModel.cancelConfirm
                    )
                }
            }
        }
        .task {
            await loader.preload([
                "point_bg", "point_icon_pay", "point_icon_star",
                "btn_icon_left", "btn_icon_right",
                "bnt_bg_blue", "btn_bg_yellow", "btn_bg_disable",
            ])
            await viewModel.load()
        }
        .onChange(of: viewModel.didFinish) { _, finished in
            if finished { onClose() }
        }
    }

    private var content: some View {
        GeometryReader { geometry in
            let available = geometry.size.height - 25

            VStack(spacing: 0) {
                Spacer().frame(height: 15.ms)

                // 0.2 — 현재 페이포인트
                ZStack {
                    if let mong = viewModel.mong {
                        PayPointBox(payPoint: mong.payPoint)
                    }
                }
                .frame(height: available * 0.2)

                // 0.5 — 좌우 버튼으로 단위를 고르고, 가운데를 위아래 반씩 나눠
                // 위에는 **남는 잔액**, 아래에는 **받게 될 페이포인트**를 그린다.
                ZStack {
                    VStack(spacing: 0) {
                        remainingRow.frame(height: available * 0.5 * 0.5)
                        gainRow.frame(height: available * 0.5 * 0.5)
                    }

                    SelectButton(
                        canGoPrevious: viewModel.units > 0,
                        canGoNext: viewModel.units < viewModel.maxUnits,
                        onPrevious: viewModel.decrease,
                        onNext: viewModel.increase
                    )
                }
                .frame(height: available * 0.5)

                // 0.3 — 환전. 원본 Row 가 `CenterVertically` 라 밴드 **가운데**에 놓인다.
                ZStack {
                    // 원본은 두 화면의 버튼 색이 다르다 —
                    // `ExchangeStepView` 는 BlueButton, `ExchangeStarPointView` 는 YellowButton.
                    MongsButton(
                        title: "환전",
                        style: viewModel.kind == .step ? .blue : .yellow,
                        width: 70,
                        // 원본은 `disable = chargePayPoint == 0` 이다 — 로딩 여부는 보지 않는다.
                        isEnabled: viewModel.canExchange,
                        action: viewModel.askConfirm
                    )
                }
                .frame(height: available * 0.3)

                Spacer().frame(height: 10.ms)
            }
            .frame(maxWidth: .infinity)
        }
    }

    /// 남는 잔액
    ///
    /// 걸음은 글자만("N 걸음"), 별가루는 아이콘 20 + 10 간격 + "x N".
    @ViewBuilder
    private var remainingRow: some View {
        HStack(spacing: 0) {
            if viewModel.kind == .starPoint {
                AnimatedSprite(sprite: loader.sprite(named: "point_icon_star"))
                    .frame(width: 20.ms, height: 20.ms)
                Spacer().frame(width: 10.ms)
                Text("x \(viewModel.remaining)")
                    .mongsFont(16)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
            } else {
                Text("\(viewModel.remaining) 걸음")
                    .mongsFont(16)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
            }
        }
    }

    /// 받게 될 페이포인트. 아이콘 크기가 화면마다 다르다 (걸음 24, 별가루 26).
    private var gainRow: some View {
        HStack(spacing: 0) {
            // 원본이 ContentScale.FillBounds 라 비율을 무시하고 프레임에 맞춘다.
            AnimatedSprite(sprite: loader.sprite(named: "point_icon_pay"), contentMode: nil)
                .frame(width: payIconSize, height: payIconSize)
            Spacer().frame(width: viewModel.kind == .step ? 8 : 10)
            Text("+ \(viewModel.payPoint)")
                .mongsFont(18)
                .foregroundStyle(MongsColor.white)
                .lineLimit(1)
        }
    }

    private var payIconSize: CGFloat { viewModel.kind == .step ? 24.ms : 26.ms }
}

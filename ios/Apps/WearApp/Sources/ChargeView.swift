import MongsModel
import MongsViewModel
import SwiftUI

/// 충전 화면
///
/// Android `pages/charge/ChargeStarPointView.kt` 이식.
/// 세로 비중 0.2(별가루) / 0.5(상품 캐러셀) / 0.3(구매 버튼).
///
/// **로딩과 내용이 배타가 아니다.** 결제 중에는 목록을 그대로 두고 위에 반투명 덮개를 씌운다.
struct ChargeView: View {

    @State private var viewModel: ChargeViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader
    @Environment(\.scenePhase) private var scenePhase

    init(viewModel: ChargeViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            DefaultBackground()

            if viewModel.phase.showsContent {
                content
            }

            if viewModel.phase.showsLoading {
                // 화면 전체를 덮어 터치를 막는다.
                //
                // 원본은 버튼 하나만 막았다가 사고를 겪었다 — 결제 직후 재조회로 "소비" 버튼이
                // 로딩바 위로 드러나면 그 탭이 진행 중인 주문의 중복 소비 요청이 됐다.
                // 클릭만 먹으므로 손목 스와이프 뒤로가기는 그대로 된다.
                Color.black.opacity(0.5)
                    .ignoresSafeArea()
                    .contentShape(Rectangle())
                    .onTapGesture {}

                LoadingBar()
            }
        }
        .task {
            await loader.preload([
                "point_bg", "point_icon_star",
                "btn_icon_left", "btn_icon_right",
                "btn_bg_yellow", "bnt_bg_blue", "btn_bg_disable",
            ])
            await viewModel.load()
        }
        // 다른 기기나 승인 대기로 끝난 결제를 돌아왔을 때 회수한다
        // (원본 `ViewLifeCycle(onResume:)`).
        .onChange(of: scenePhase) { _, phase in
            if phase == .active { Task { await viewModel.refresh() } }
        }
        .onChange(of: viewModel.shouldClose) { _, close in
            if close { onClose() }
        }
    }

    private var content: some View {
        GeometryReader { geometry in
            // 위 15dp 를 뺀 남은 공간에 비율을 건다.
            let available = geometry.size.height - 15

            ZStack {
                VStack(spacing: 0) {
                    Spacer().frame(height: 15.ms)

                    // 0.2 — 현재 별가루
                    ZStack { StarPointBox(starPoint: viewModel.starPoint) }
                        .frame(height: available * 0.2)

                    // 0.5 — 상품 이름 + 별 아이콘, 좌우로 넘긴다
                    ZStack {
                        VStack(spacing: 0) {
                            Text(viewModel.current?.productName ?? "")
                                .mongsFont(16)
                                .foregroundStyle(MongsColor.white)
                                .lineLimit(1)

                            Spacer().frame(height: 15.ms)

                            AnimatedSprite(sprite: loader.sprite(named: "point_icon_star"))
                                .frame(width: 28.ms, height: 28.ms)
                        }

                        SelectButton(
                            canGoPrevious: !viewModel.isFirst,
                            canGoNext: !viewModel.isLast,
                            onPrevious: viewModel.previous,
                            onNext: viewModel.next
                        )
                    }
                    .frame(height: available * 0.5)

                    // 0.3 — 미소비 주문이 있으면 "소비", 없으면 가격 버튼
                    ZStack { purchaseButton }
                        .frame(height: available * 0.3)
                }
                .frame(maxWidth: .infinity)

                if viewModel.products.count > 1 {
                    VStack {
                        Spacer()
                        PageIndicator(pageCount: viewModel.products.count, currentPage: viewModel.index)
                            .padding(.bottom, 5.ms)
                    }
                }
            }
        }
    }

    /// 결제가 끝났는데 지급이 안 된 주문이 남아 있으면 **구매 대신 소비**를 띄운다.
    /// 자동 회수가 실패했을 때 사용자가 직접 되찾는 폴백이다.
    @ViewBuilder
    private var purchaseButton: some View {
        if let product = viewModel.current {
            if product.orders.isEmpty {
                MongsButton(
                    title: product.priceText, style: .yellow,
                    width: 110, height: 37, fontSize: 16
                ) {
                    Task { await viewModel.purchase() }
                }
            } else {
                MongsButton(
                    title: "소비", style: .blue,
                    width: 90, height: 37, fontSize: 16
                ) {
                    Task { await viewModel.consumePending() }
                }
            }
        }
    }
}

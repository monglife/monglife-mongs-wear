import MongsModel
import MongsViewModel
import SwiftUI

/// 걸음 쪽
///
/// Android `.../pages/main/StepContent.kt` 이식.
/// 세로 비중이 정해져 있다 — 페이포인트 0.2 / 걸음 수 0.5 / 환전 버튼 0.3.
struct StepContentView: View {

    @State private var viewModel: MainStepViewModel
    /// 페이포인트는 몽에 딸린 값이라 슬롯 쪽에서 받아온다.
    var mong: Mong?

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: MainStepViewModel, mong: Mong? = nil) {
        _viewModel = State(initialValue: viewModel)
        self.mong = mong
    }

    var body: some View {
        GeometryReader { geometry in
            // Compose 의 `weight` 는 고정 크기(Spacer 15dp)를 뺀 **남은 공간**에 비율을 적용한다.
            // 전체 높이에 비율을 걸면 원본과 밴드 위치가 어긋난다.
            let available = geometry.size.height - 15

            VStack(spacing: 0) {
                Spacer().frame(height: 15)

                // 0.2 — 페이포인트
                ZStack {
                    if let mong {
                        PayPointBox(payPoint: mong.payPoint)
                    }
                }
                .frame(height: available * 0.2)

                // 0.5 — 걸음 수
                ZStack {
                    // available 이 false 면 0 이 아니라 "-" 다.
                    // 수집 경로가 정해지기 전의 0 은 "0걸음"이 아니라 "모름"이기 때문이다.
                    Text(viewModel.step.available ? "\(viewModel.step.currentWalkingCount) 걸음" : "-")
                        .mongsFont(18)
                        .foregroundStyle(MongsColor.white)
                        .lineLimit(1)
                        .contentTransition(.numericText())
                        .animation(.default, value: viewModel.step.currentWalkingCount)
                }
                .frame(height: available * 0.5)

                // 0.3 — 환전 (위 정렬)
                VStack {
                    if mong != nil {
                        // 원본은 걸음 수와 무관하게 몽 상태(DEAD/DELETE)로만 잠근다 —
                        // 버튼이 환전 화면으로 넘어가는 메뉴 항목이고, 금액 판단은 그 화면이 한다.
                        // 환전 화면은 아직 없어서 여기서 바로 환전한다.
                        MongsButton(
                            title: "환전",
                            style: .blue,
                            width: 70,
                            isEnabled: mong?.isInteractable == true
                        ) {
                            Task { await viewModel.exchange() }
                        }
                    }
                    Spacer(minLength: 0)
                }
                .frame(height: available * 0.3)
            }
            .frame(maxWidth: .infinity)
        }
        .task {
            await loader.preload(["point_bg", "point_icon_pay", "bnt_bg_blue", "btn_bg_disable"])
        }
        .task { await viewModel.observeWalkingCount() }
    }
}

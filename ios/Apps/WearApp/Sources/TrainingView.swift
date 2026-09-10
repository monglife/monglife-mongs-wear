import MongsModel
import MongsViewModel
import SwiftUI

/// 훈련 (미니게임 공통 껍데기)
///
/// Android `pages/training/Training*Content.kt` 3개의 공통 부분 이식.
/// 원본은 종목마다 화면을 따로 뒀지만 **다른 건 가운데 플레이 영역뿐**이라
/// 참가 → 플레이 → 결과 흐름을 하나로 모았다.
struct TrainingView: View {

    @State private var viewModel: TrainingViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: TrainingViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            // 원본은 훈련 전용 배경(`bg_training`)을 좌우로 이어 붙여 스크롤시킨다.
            // 여기서는 한 장을 채워 그린다 — 스크롤 연출은 화면 디테일 단계에서 본다.
            AnimatedSprite(sprite: loader.sprite(named: "bg_training"), contentMode: .fill)
                .ignoresSafeArea()

            switch viewModel.phase {
            case .loading:
                LoadingBar()

            case .entering:
                if let type = viewModel.type {
                    TrainingEnteringDialog(
                        type: type,
                        canEnter: viewModel.canEnter,
                        onStart: viewModel.begin
                    )
                }

            case .playing:
                playing

            case let .over(result):
                TrainingOverDialog(result: result) { viewModel.closeResult() }
            }
        }
        .task {
            await loader.preload(["bg_training", "point_icon_pay"])
            await viewModel.load()
        }
        .onDisappear { viewModel.cancelTimer() }
        .onChange(of: viewModel.shouldClose) { _, close in
            if close { onClose() }
        }
    }

    @ViewBuilder
    private var playing: some View {
        ZStack {
            game

            // 점수와 남은 시간은 항상 위에 얹는다.
            //
            // ⚠️ 화면 맨 위 양쪽은 **비워 둔다.** 왼쪽엔 watchOS 의 닫기(X) 버튼이,
            // 오른쪽엔 시계가 겹친다 — Android 에는 둘 다 없어서 원본은 위쪽 모서리를 쓴다.
            // 그대로 옮기면 점수가 X 뒤로 숨는다.
            VStack {
                HStack(spacing: 10.ms) {
                    Text("\(viewModel.score)")
                        .mongsFont(16)
                        .foregroundStyle(MongsColor.white)

                    if let remaining = viewModel.remainingSeconds {
                        Text("\(remaining)s")
                            .mongsFont(16)
                            .foregroundStyle(remaining <= 5 ? MongsColor.red : MongsColor.white)
                    }
                }
                .padding(.horizontal, 8.ms)
                .padding(.vertical, 3.ms)
                .background(Capsule().fill(Color.black.opacity(0.45)))
                .padding(.top, 34.ms)

                Spacer()
            }
        }
    }

    @ViewBuilder
    private var game: some View {
        switch viewModel.type?.game {
        case .runner:
            RunnerGameView(
                onScore: { viewModel.addScore($0) },
                onGameOver: { Task { await viewModel.finish() } }
            )
        case .basketball:
            BasketballGameView(onScore: { viewModel.addScore($0) })
        case .rockPaperScissors:
            RockPaperScissorsGameView(onScore: { viewModel.addScore($0) })
        case nil:
            // 화면이 없는 종목은 목록에서 걸러지므로 여기 오지 않는다.
            EmptyView()
        }
    }
}

/// 훈련 메뉴 → 종목 선택 → 플레이까지의 껍데기
///
/// 메뉴는 목록을 먼저 받아야 그릴 수 있어서 여기서 한 번 불러온다.
struct TrainingFlowView: View {

    let onClose: () -> Void

    @Environment(AppContainer.self) private var container
    @State private var types: [TrainingType] = []
    @State private var isLoading = true
    @State private var selected: TrainingType?

    var body: some View {
        ZStack {
            DefaultBackground()

            if isLoading {
                LoadingBar()
            } else {
                TrainingMenuView(types: types) { selected = $0 }
            }
        }
        .fullScreenCover(item: $selected) { type in
            if let viewModel = container.makeTrainingViewModel(code: type.trainingCode) {
                TrainingView(viewModel: viewModel) { selected = nil }
            }
        }
        .task {
            defer { isLoading = false }
            types = await container.trainingTypes()
            if types.isEmpty { onClose() }
        }
    }
}

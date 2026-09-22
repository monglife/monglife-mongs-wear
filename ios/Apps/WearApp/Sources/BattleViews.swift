import MongsModel
import MongsViewModel
import SwiftUI

/// 배틀 메뉴
///
/// Android `pages/battle/BattleMenuView.kt` 이식.
/// 배팅 포인트를 보여주고, 시작하면 상대를 찾는다.
struct BattleMenuView: View {

    @State private var viewModel: BattleMenuViewModel
    let onClose: () -> Void

    @Environment(AppContainer.self) private var container
    @Environment(SpriteLoader.self) private var loader
    @State private var match: MatchQueue?

    init(viewModel: BattleMenuViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            AnimatedSprite(sprite: loader.sprite(named: "bg_battle_gif"), contentMode: .fill)
                .ignoresSafeArea()

            switch viewModel.phase {
            case .loading:
                LoadingBar()
            case .idle:
                idle
            case .matching:
                matching
            case .cancelConfirm:
                ConfirmDialogView(
                    message: "매칭을\n취소하시겠습니까?",
                    onConfirm: { Task { await viewModel.cancelMatching() } },
                    onCancel: viewModel.dismissCancel
                )
            }
        }
        .task {
            await loader.preload(["bg_battle_gif", "txt_battle", "point_icon_pay", "icon_loading"])
            await viewModel.load()
        }
        .onDisappear { viewModel.stop() }
        .onChange(of: viewModel.matched?.matchId) { _, _ in
            guard let queue = viewModel.matched else { return }
            match = queue
            viewModel.consumeMatch()
        }
        .onChange(of: viewModel.shouldClose) { _, close in
            if close { onClose() }
        }
        .fullScreenCover(item: Binding(get: { match }, set: { match = $0 })) { queue in
            if let viewModel = container.makeBattleMatchViewModel(queue: queue) {
                BattleMatchView(viewModel: viewModel) { match = nil }
            }
        }
    }

    private var idle: some View {
        VStack(spacing: 10.ms) {
            AnimatedSprite(sprite: loader.sprite(named: "txt_battle"), contentMode: nil)
                .frame(width: 110.ms, height: 40.ms)

            if let outcome = viewModel.outcome {
                HStack(spacing: 10.ms) {
                    AnimatedSprite(sprite: loader.sprite(named: "point_icon_pay"))
                        .frame(width: 20.ms, height: 20.ms)
                    Text("- \(outcome.battingPayPoint)")
                        .mongsFont(16)
                        .foregroundStyle(MongsColor.white)
                        .lineLimit(1)
                }

                Text("이기면 + \(outcome.rewardPayPoint)")
                    .mongsFont(11)
                    .foregroundStyle(MongsColor.lightGray)
                    .lineLimit(1)
            }

            MongsButton(
                title: "매칭", style: .blue, width: 90, height: 37, fontSize: 16,
                isEnabled: viewModel.canEnter
            ) {
                Task { await viewModel.startMatching() }
            }

            if !viewModel.canEnter {
                Text("페이포인트가 부족해요")
                    .mongsFont(11)
                    .foregroundStyle(MongsColor.red)
                    .lineLimit(1)
            }
        }
    }

    private var matching: some View {
        VStack(spacing: 12.ms) {
            LoadingBar()
            Text("상대를 찾는 중")
                .mongsFont(13)
                .foregroundStyle(MongsColor.lightGray)
                .lineLimit(1)
            MongsButton(title: "취소", style: .blue, width: 70, action: viewModel.askCancel)
        }
    }
}

/// 배틀 매치
///
/// Android `pages/battle/BattleMatchView.kt` + 다이얼로그 2개 이식.
/// 위아래로 상대와 나를 놓고, 라운드마다 공격·방어·회복 중 하나를 고른다.
struct BattleMatchView: View {

    @State private var viewModel: BattleMatchViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: BattleMatchViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            AnimatedSprite(sprite: loader.sprite(named: "bg_battle_gif"), contentMode: .fill)
                .ignoresSafeArea()

            if case let .over(winner) = viewModel.phase {
                overDialog(winner)
            } else {
                arena

                switch viewModel.phase {
                case .entering:
                    ZStack {
                        Color.black.opacity(0.7).ignoresSafeArea()
                        VStack(spacing: 10.ms) {
                            AnimatedSprite(sprite: loader.sprite(named: "txt_vs"), contentMode: nil)
                                .frame(width: 80.ms, height: 40.ms)
                            Text("입장 중")
                                .mongsFont(12)
                                .foregroundStyle(MongsColor.lightGray)
                        }
                    }
                case .pick:
                    pickDialog
                case .pickWaiting:
                    VStack {
                        Spacer()
                        HStack(spacing: 6.ms) {
                            LoadingBar(size: 20)
                            Text("상대를 기다리는 중")
                                .mongsFont(11)
                                .foregroundStyle(MongsColor.lightGray)
                        }
                        .padding(.bottom, 10.ms)
                    }
                case .loading:
                    LoadingBar()
                default:
                    EmptyView()
                }
            }
        }
        .task {
            await loader.preload([
                "bg_battle_gif", "txt_vs", "txt_me", "txt_win", "txt_lose",
                "effect_attack", "effect_defence", "icon_loading", "point_icon_pay",
            ])
            await viewModel.start()
        }
        // ⚠️ 화면을 떠나면 반드시 퇴장을 알린다 — 안 보내면 상대가 끝까지 기다린다.
        .onDisappear { Task { await viewModel.leave() } }
        .onChange(of: viewModel.shouldClose) { _, close in
            if close { onClose() }
        }
    }

    /// 위: 상대, 아래: 나.
    private var arena: some View {
        VStack(spacing: 0) {
            playerRow(viewModel.opponent, maxHp: viewModel.opponentMaxHp, isMe: false)
                .frame(maxHeight: .infinity)

            if let match = viewModel.match {
                Text("ROUND \(match.round) / \(BattleMatchViewModel.maxRound)")
                    .mongsFont(11)
                    .foregroundStyle(MongsColor.lightGray)
                    .lineLimit(1)
            }

            playerRow(viewModel.me, maxHp: viewModel.myMaxHp, isMe: true)
                .frame(maxHeight: .infinity)
        }
        .padding(.vertical, 20.ms)
    }

    @ViewBuilder
    private func playerRow(_ player: Match.Player?, maxHp: Double, isMe: Bool) -> some View {
        if let player {
            VStack(spacing: 4.ms) {
                if isMe {
                    AnimatedSprite(sprite: loader.sprite(named: "txt_me"), contentMode: nil)
                        .frame(width: 30.ms, height: 12.ms)
                }

                ZStack {
                    MongView(code: player.resource, bodySize: 60, isAnimated: false)

                    // 라운드 결과 이펙트. 원본도 같은 자리에 얹는다.
                    if let effect = Self.effect(for: player.roundCode) {
                        AnimatedSprite(sprite: loader.sprite(named: effect), contentMode: nil)
                            .frame(width: 60.ms, height: 60.ms)
                    }
                }

                HpBar(hp: player.hp, maxHp: maxHp)

                Text(player.name)
                    .mongsFont(10)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
            }
        }
    }

    private static func effect(for code: MatchRoundCode) -> String? {
        switch code {
        case .attacked, .attackedHeal: "effect_attack"
        case .defence: "effect_defence"
        default: nil
        }
    }

    /// 선택 다이얼로그
    ///
    /// Android `dialog/pages/battle/MatchPickDialog.kt` 이식.
    private var pickDialog: some View {
        ZStack {
            Color.black.opacity(0.85).ignoresSafeArea()

            VStack(spacing: 10.ms) {
                Text("무엇을 할까요")
                    .mongsFont(13)
                    .foregroundStyle(MongsColor.lightGray)
                    .lineLimit(1)

                HStack(spacing: 5.ms) {
                    ForEach(MatchPickCode.allCases) { code in
                        MongsButton(title: code.title, style: .blue, width: 56, height: 34) {
                            Task { await viewModel.pick(code) }
                        }
                    }
                }
            }
        }
    }

    /// 결과 다이얼로그
    ///
    /// Android `dialog/pages/battle/MatchOverDialog.kt` 이식.
    private func overDialog(_ winner: MatchWinner) -> some View {
        let didWin = viewModel.me?.playerId == winner.playerId

        return ZStack {
            Color.black.opacity(0.9).ignoresSafeArea()

            VStack(spacing: 8.ms) {
                AnimatedSprite(sprite: loader.sprite(named: didWin ? "txt_win" : "txt_lose"),
                               contentMode: nil)
                    .frame(width: 90.ms, height: 35.ms)

                MongView(code: winner.resource, bodySize: 60, isAnimated: false)

                Text(winner.name)
                    .mongsFont(13)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)

                HStack(spacing: 10.ms) {
                    AnimatedSprite(sprite: loader.sprite(named: "point_icon_pay"))
                        .frame(width: 20.ms, height: 20.ms)
                    Text("+ \(winner.rewardPayPoint)")
                        .mongsFont(16)
                        .foregroundStyle(MongsColor.white)
                        .lineLimit(1)
                }

                MongsButton(title: "종료", style: .blue, width: 80, action: viewModel.close)
            }
        }
    }
}

/// HP 바
///
/// Android `component/pages/battle/HpBar.kt` 이식.
struct HpBar: View {

    let hp: Double
    let maxHp: Double

    private var ratio: Double {
        guard maxHp > 0 else { return 0 }
        return min(max(hp / maxHp, 0), 1)
    }

    var body: some View {
        ZStack(alignment: .leading) {
            Capsule().fill(Color.black.opacity(0.5))
            Capsule()
                .fill(ratio > 0.3 ? MongsColor.green : MongsColor.red)
                .frame(width: 70.ms * ratio)
        }
        .frame(width: 70.ms, height: 6.ms)
    }
}

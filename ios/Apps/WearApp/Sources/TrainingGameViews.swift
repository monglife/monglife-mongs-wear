import MongsModel
import MongsViewModel
import SwiftUI

/// 달리기
///
/// Android `pages/training/TrainingRunnerContent.kt` + `RunnerSection.kt` 이식.
/// 화면 아무 곳이나 탭하면 점프한다. 장애물(똥)에 부딪히면 끝.
struct RunnerGameView: View {

    let onScore: (Int) -> Void
    let onGameOver: () -> Void

    @Environment(SpriteLoader.self) private var loader
    @State private var engine: RunnerEngine?
    @State private var didReportGameOver = false

    var body: some View {
        GeometryReader { geometry in
            let size = geometry.size
            // 바닥은 화면 아래에서 조금 띄운다.
            let groundY = size.height * 0.62

            ZStack(alignment: .topLeading) {
                if let engine {
                    // 플레이어 (몽 대신 러너 아이콘 — 원본도 아이콘을 쓴다)
                    AnimatedSprite(sprite: loader.sprite(named: "btn_icon_runner"))
                        .frame(width: engine.player.width, height: engine.player.height)
                        .offset(x: engine.player.x, y: engine.player.y - engine.player.height)

                    ForEach(engine.hurdles) { hurdle in
                        AnimatedSprite(sprite: loader.sprite(named: "icon_poop"))
                            .frame(width: hurdle.width, height: hurdle.height)
                            .offset(x: hurdle.x, y: hurdle.y - hurdle.height)
                    }

                    // 바닥선
                    Rectangle()
                        .fill(MongsColor.white.opacity(0.5))
                        .frame(width: size.width, height: 2.ms)
                        .offset(y: groundY)
                }
            }
            .frame(width: size.width, height: size.height)
            .contentShape(Rectangle())
            .onTapGesture { engine?.jump() }
            .task {
                await loader.preload(["btn_icon_runner", "icon_poop"])
                var created = RunnerEngine(
                    playerWidth: 34.ms, playerHeight: 34.ms,
                    playerX: size.width * 0.18,
                    groundY: groundY, startX: -60, endX: size.width + 20
                )
                created.start()
                engine = created
                await run()
            }
        }
    }

    /// 60fps 로 엔진을 돌린다.
    ///
    /// 엔진은 값 타입이라 매 틱 통째로 갈아끼운다 — SwiftUI 가 그 변화를 보고 다시 그린다.
    private func run() async {
        while !Task.isCancelled {
            try? await Task.sleep(for: RunnerEngine.tickDuration)
            guard var current = engine else { return }

            let previousScore = current.score
            let over = current.tick()
            engine = current

            if current.score > previousScore {
                onScore(current.score - previousScore)
            }
            if over, !didReportGameOver {
                didReportGameOver = true
                // 착지 연출이 보일 시간을 주고 끝낸다 (원본의 두 번째 while).
                try? await Task.sleep(for: .milliseconds(600))
                onGameOver()
                return
            }
        }
    }
}

/// 농구
///
/// Android `pages/training/TrainingBasketballContent.kt` 이식.
/// 아래에서 위로 끌어(드래그) 던진다. 골대에 넣으면 점수.
struct BasketballGameView: View {

    let onScore: (Int) -> Void

    @Environment(SpriteLoader.self) private var loader
    @State private var engine: BasketballEngine?
    @State private var dragEnd: CGPoint?

    var body: some View {
        GeometryReader { geometry in
            let size = geometry.size
            let ballStart = CGPoint(x: size.width / 2, y: size.height * 0.82)
            let basketY = size.height * 0.3

            ZStack(alignment: .topLeading) {
                if let engine {
                    AnimatedSprite(sprite: loader.sprite(named: "icon_basket"), contentMode: nil)
                        .frame(width: engine.basket.width, height: engine.basket.height)
                        .offset(
                            x: engine.basket.x - engine.basket.width / 2,
                            y: engine.basket.y - engine.basket.height / 2
                        )

                    Circle()
                        .fill(MongsColor.yellow)
                        .frame(width: engine.ball.radius * 2, height: engine.ball.radius * 2)
                        .rotationEffect(.degrees(engine.ball.degree))
                        .offset(
                            x: engine.ball.x - engine.ball.radius,
                            y: engine.ball.y - engine.ball.radius
                        )
                }
            }
            .frame(width: size.width, height: size.height)
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 8)
                    .onEnded { value in
                        // 끌어 놓은 지점이 방향이 된다. 세기는 원본처럼 고정이다.
                        engine?.throwBall(vx: value.location.x, vy: value.location.y)
                    }
            )
            .task {
                await loader.preload(["icon_basket"])
                var created = BasketballEngine(
                    ball: .init(
                        x: ballStart.x, y: ballStart.y,
                        minRadius: 6.ms, maxRadius: 14.ms
                    ),
                    basket: .init(
                        width: 44.ms, height: 20.ms,
                        x: size.width / 2, y: basketY
                    )
                )
                created.start()
                engine = created
                await run()
            }
        }
    }

    private func run() async {
        while !Task.isCancelled {
            try? await Task.sleep(for: BasketballEngine.tickDuration)
            guard var current = engine else { return }

            let previousScore = current.score
            current.tick()
            engine = current

            if current.score > previousScore {
                onScore(current.score - previousScore)
            }
        }
    }
}

/// 가위바위보
///
/// Android `pages/training/TrainingRockPaperScissorsContent.kt` +
/// `RockPaperScissorsPickDialog.kt` 이식.
/// 셋 중 하나를 고르면 상대가 무작위로 낸다. 이기면 점수.
struct RockPaperScissorsGameView: View {

    let onScore: (Int) -> Void

    @Environment(SpriteLoader.self) private var loader
    @State private var mine: RockPaperScissors?
    @State private var theirs: RockPaperScissors?
    @State private var outcome: RockPaperScissors.Outcome?
    @State private var isResolving = false

    var body: some View {
        VStack(spacing: 8.ms) {
            // 결과 영역
            ZStack {
                if let mine, let theirs {
                    HStack(spacing: 14.ms) {
                        hand(mine)
                        Text("vs")
                            .mongsFont(12)
                            .foregroundStyle(MongsColor.lightGray)
                        hand(theirs)
                    }
                } else {
                    Text("무엇을 낼까요")
                        .mongsFont(13)
                        .foregroundStyle(MongsColor.lightGray)
                        .lineLimit(1)
                }
            }
            .frame(maxHeight: .infinity)

            Text(outcomeText)
                .mongsFont(14)
                .foregroundStyle(outcomeColor)
                .lineLimit(1)

            // 선택 버튼 셋
            HStack(spacing: 6.ms) {
                ForEach(RockPaperScissors.allCases) { choice in
                    Button { pick(choice) } label: {
                        AnimatedSprite(sprite: loader.sprite(named: sprite(for: choice)))
                            .frame(width: 34.ms, height: 34.ms)
                    }
                    .buttonStyle(.plain)
                    .disabled(isResolving)
                    .opacity(isResolving ? 0.4 : 1)
                }
            }
            .padding(.bottom, 8.ms)
        }
        .task { await loader.preload(["icon_rock", "icon_paper", "icon_scissors"]) }
    }

    private var outcomeText: String {
        switch outcome {
        case .win: "이겼다!"
        case .lose: "졌다"
        case .draw: "비겼다"
        case nil: " "
        }
    }

    private var outcomeColor: Color {
        switch outcome {
        case .win: MongsColor.green
        case .lose: MongsColor.red
        default: MongsColor.lightGray
        }
    }

    private func hand(_ choice: RockPaperScissors) -> some View {
        AnimatedSprite(sprite: loader.sprite(named: sprite(for: choice)))
            .frame(width: 44.ms, height: 44.ms)
    }

    private func sprite(for choice: RockPaperScissors) -> String {
        switch choice {
        case .rock: "icon_rock"
        case .paper: "icon_paper"
        case .scissors: "icon_scissors"
        }
    }

    private func pick(_ choice: RockPaperScissors) {
        guard !isResolving else { return }
        isResolving = true

        let opponent = RockPaperScissors.random()
        mine = choice
        theirs = opponent

        let result = choice.result(against: opponent)
        outcome = result
        if result == .win { onScore(1) }

        // 결과를 잠깐 보여주고 다음 판으로 넘어간다.
        Task {
            try? await Task.sleep(for: .milliseconds(900))
            outcome = nil
            mine = nil
            theirs = nil
            isResolving = false
        }
    }
}

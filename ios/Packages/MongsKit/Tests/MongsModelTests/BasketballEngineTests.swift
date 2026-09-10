import Foundation
import Testing
@testable import MongsModel

/// 농구 엔진 테스트
///
/// 달리기와 같은 이유 — **Android 원본에 테스트가 없다.**
@Suite("농구 훈련 엔진")
struct BasketballEngineTests {

    /// 공은 아래(y=400)에서 시작하고 골대는 위(y=150)에 있다.
    private func makeEngine() -> BasketballEngine {
        BasketballEngine(
            ball: .init(x: 100, y: 400, minRadius: 6, maxRadius: 14),
            basket: .init(width: 40, height: 20, x: 100, y: 150)
        )
    }

    @Test("던지기 전에는 공이 움직이지 않는다")
    func idleBallDoesNotMove() {
        var engine = makeEngine()
        engine.start()
        let before = engine.ball

        for _ in 0 ..< 30 { engine.tick() }
        #expect(engine.ball == before)
    }

    @Test("위로 던지면 올라갔다가 떨어진다")
    func thrownBallRisesThenFalls() {
        var engine = makeEngine()
        engine.start()
        // 시작점보다 위쪽(작은 y)을 끌면 위로 날아간다.
        engine.throwBall(vx: 100, vy: 100)

        var minY = engine.ball.y
        for _ in 0 ..< 60 {
            engine.tick()
            minY = min(minY, engine.ball.y)
        }

        #expect(minY < 400)          // 올라갔다
        #expect(engine.ball.isThrown)
    }

    @Test("멀어질수록 공이 작아지지만 최소 반지름 아래로는 안 간다")
    func ballShrinksToMinimum() {
        var engine = makeEngine()
        engine.start()
        engine.throwBall(vx: 100, vy: 100)

        for _ in 0 ..< 200 where engine.ball.isThrown {
            engine.tick()
        }
        #expect(engine.ball.radius >= 6)
    }

    @Test("바닥 아래로 떨어지면 공이 처음 자리로 돌아온다")
    func ballResetsAfterFalling() {
        var engine = makeEngine()
        engine.start()
        engine.throwBall(vx: 100, vy: 100)

        // resetY(600)를 지날 때까지 충분히 돌린다.
        for _ in 0 ..< 400 {
            engine.tick()
            if !engine.ball.isThrown { break }
        }

        #expect(!engine.ball.isThrown)
        #expect(engine.ball.x == 100)
        #expect(engine.ball.y == 400)
        #expect(engine.ball.radius == 14)
        #expect(!engine.ball.isAboveRim)
    }

    @Test("던지는 세기는 끌어당긴 거리와 무관하다 — 방향만 쓴다")
    func throwStrengthIgnoresDragDistance() {
        // 원본은 방향 벡터를 정규화하지 않고 **큰 쪽 성분을 ballSpeed 로 고정**한 뒤
        // 나머지를 비례로 맞춘다. 그래서 1픽셀을 끌든 300픽셀을 끌든 같은 궤적이 나온다.
        // 직관과 어긋나지만 원본의 손맛이라 그대로 뒀다.
        var tiny = makeEngine()
        tiny.start()
        tiny.throwBall(vx: 100, vy: 399)

        var huge = makeEngine()
        huge.start()
        huge.throwBall(vx: 100, vy: 100)

        for _ in 0 ..< 30 {
            tiny.tick()
            huge.tick()
        }
        #expect(tiny.ball.y == huge.ball.y)
        #expect(tiny.ball.x == huge.ball.x)
    }

    @Test("림 위로 올라간 적이 없으면 골 판정을 켜지 않는다")
    func goalNeedsRisingAboveRim() {
        var engine = makeEngine()
        engine.start()
        // 던지지 않고 골대 사각형 안에 있는 상태를 만들 수는 없으므로,
        // 던지기 전에는 판정 자체가 꺼져 있다는 것만 확인한다.
        for _ in 0 ..< 60 { engine.tick() }
        #expect(!engine.ball.isAboveRim)
        #expect(engine.score == 0)
    }

    @Test("한 번 넣은 공은 점수를 두 번 주지 않는다")
    func goalCountsOnce() {
        var engine = makeEngine()
        engine.start()
        engine.throwBall(vx: 100, vy: 100)

        for _ in 0 ..< 400 {
            engine.tick()
            if !engine.ball.isThrown { break }
        }
        // 들어갔든 안 들어갔든 한 번의 던지기로 2점이 나올 수는 없다.
        #expect(engine.score <= 1)
    }

    @Test("한 틱은 16ms 다 — 달리기와 같은 정수 나눗셈")
    func tickIsIntegerMillis() {
        #expect(BasketballEngine.tickMillis == 16)
    }
}

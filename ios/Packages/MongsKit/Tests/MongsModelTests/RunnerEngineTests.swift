import Foundation
import Testing
@testable import MongsModel

/// 달리기 엔진 테스트
///
/// **Android 원본에는 테스트가 하나도 없다.** 게임 규칙이 코드에만 있어서
/// 이식이 맞는지 확인할 방법이 없었다 — 그래서 여기서 규칙을 고정한다.
@Suite("달리기 훈련 엔진")
struct RunnerEngineTests {

    /// 화면과 무관하게 재현되도록 고정한 값들.
    private func makeEngine() -> RunnerEngine {
        RunnerEngine(
            playerWidth: 40, playerHeight: 40, playerX: 30,
            groundY: 100, startX: 0, endX: 200
        )
    }

    @Test("점프하면 위로 올라갔다가 지면으로 돌아온다")
    func jumpReturnsToGround() {
        var engine = makeEngine()
        engine.start()
        engine.jump()

        #expect(engine.player.isJumping)

        var minY = engine.player.y
        // 충분히 돌려서 착지까지 본다.
        for _ in 0 ..< 200 {
            engine.tick()
            minY = min(minY, engine.player.y)
            if !engine.player.isJumping { break }
        }

        // y 는 아래로 크다 — 올라갔다는 건 값이 작아졌다는 뜻이다.
        #expect(minY < 100)
        #expect(engine.player.y == 100)
        #expect(!engine.player.isJumping)
    }

    @Test("점프 중에 다시 점프해도 더 높이 뜨지 않는다")
    func doubleJumpIsIgnored() {
        var engine = makeEngine()
        engine.start()
        engine.jump()
        engine.tick()

        let afterFirst = engine.player.y
        engine.jump()   // 무시돼야 한다

        // 무시되지 않았다면 y 가 지면으로 되돌아간다(jump() 가 py 를 초기화한다).
        #expect(engine.player.y == afterFirst)
    }

    /// 생성 주기를 넘기는 데 필요한 틱 수.
    /// 한 틱은 16ms 라(정수 나눗셈) 1500ms 를 넘기려면 94틱이다 — 90틱이 아니다.
    private var ticksPerHurdle: Int {
        RunnerEngine.Constants.hurdleGenDelayMillis / RunnerEngine.tickMillis + 1
    }

    @Test("장애물은 생성 주기마다 하나씩 나온다")
    func hurdlesSpawnOnSchedule() {
        // 왼쪽 끝을 멀리 둬서 생성된 장애물이 정리되지 않게 한다.
        // (정리까지 함께 보면 두 번째 주기에 첫 장애물이 이미 사라져 개수가 1 로 남는다.)
        var engine = RunnerEngine(
            playerWidth: 40, playerHeight: 40, playerX: -500,
            groundY: 100, startX: -10000, endX: 200
        )
        engine.start()

        for _ in 0 ..< ticksPerHurdle { engine.tick() }
        #expect(engine.hurdles.count == 1)

        for _ in 0 ..< ticksPerHurdle { engine.tick() }
        #expect(engine.hurdles.count == 2)
    }

    @Test("한 틱은 16ms 다 — 정수 나눗셈이라 게임 시계가 벽시계보다 느리다")
    func tickIsIntegerMillis() {
        #expect(RunnerEngine.tickMillis == 16)
    }

    @Test("지면에 서 있으면 다가온 장애물과 충돌한다")
    func collisionOnGround() {
        var engine = makeEngine()
        engine.start()

        var didCollide = false
        // 장애물이 생성되고 플레이어까지 도달할 만큼 돌린다.
        for _ in 0 ..< 400 where !didCollide {
            didCollide = engine.tick()
        }

        #expect(didCollide)
        #expect(!engine.isRunning)
    }

    @Test("지나간 장애물은 목록에서 사라진다")
    func hurdlesAreCleared() {
        // 플레이어를 화면 밖에 두어 충돌하지 않게 한다.
        var engine = RunnerEngine(
            playerWidth: 10, playerHeight: 10, playerX: -500,
            groundY: 100, startX: 0, endX: 200
        )
        engine.start()

        for _ in 0 ..< ticksPerHurdle { engine.tick() }
        #expect(engine.hurdles.count == 1)

        // 200 → 0 까지 speed 3 이면 약 67틱. 다음 생성 전에 사라진다.
        for _ in 0 ..< 70 { engine.tick() }
        #expect(engine.hurdles.isEmpty)
    }

    @Test("장애물 하나는 점수를 한 번만 준다")
    func scoreCountsOnce() {
        var engine = RunnerEngine(
            playerWidth: 40, playerHeight: 40, playerX: 30,
            groundY: 100, startX: 0, endX: 200
        )
        engine.start()

        // 충돌하지 않도록 매 틱 점프를 시도한다 (착지하면 바로 다시 뛴다).
        for _ in 0 ..< 300 {
            engine.jump()
            if engine.tick() { break }
        }

        // 장애물이 두 개는 지나갔지만 점수는 장애물 수를 넘지 않는다.
        #expect(engine.score <= 3)
    }

    @Test("게임이 끝나도 점프 중이면 착지까지 그린다")
    func fallsAfterGameOver() {
        var engine = makeEngine()
        engine.start()
        engine.jump()
        engine.tick()
        engine.stop()

        #expect(engine.player.isJumping)

        for _ in 0 ..< 200 where engine.player.isJumping {
            engine.tick()
        }
        #expect(!engine.player.isJumping)
        #expect(engine.player.y == 100)
    }
}

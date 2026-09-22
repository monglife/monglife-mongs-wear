import Foundation

/// 달리기 훈련 엔진
///
/// Android `presentation/.../training/runner/engine/{RunnerEngine,Runner,RunnerPlayer,RunnerHurdle}.kt`
/// (409 LOC) 이식.
///
/// **원본은 Presentation 레이어에 있지만 여기서는 `MongsModel` 에 둔다.**
/// 화면도 서버도 모르는 순수 계산이라 도메인이 맞는 자리고, 무엇보다
/// **원본에 테스트가 하나도 없어서** 시뮬레이터 없이 돌릴 수 있는 곳에 둬야 검증이 된다.
///
/// 좌표계는 화면과 같다 — **y 는 아래로 갈수록 크다.** 그래서 점프는 `py` 를 줄인다.
public struct RunnerEngine: Sendable {

    /// Android `RunnerEngine` 의 상수들. 값을 바꾸면 체감 난이도가 통째로 달라진다.
    public enum Constants {
        public static let gravity: Double = 9.8
        /// 초당 프레임. 한 틱은 `1000 / frame` 밀리초다.
        public static let frame: Int = 60
        public static let playerSpeed: Double = 42
        public static let hurdleSpeed: Double = 3
        /// 장애물 생성 주기(밀리초)
        public static let hurdleGenDelayMillis: Int = 1500
        /// 충돌 판정을 이 값만큼 안쪽으로 줄인다. 스프라이트 여백 때문에
        /// 그림이 닿기 전에 죽는 것처럼 보이는 걸 막는다.
        public static let collisionPadding: Double = 10
    }

    // MARK: - 플레이어

    public struct Player: Sendable, Equatable {
        public let height: Double
        public let width: Double
        public private(set) var x: Double
        public private(set) var y: Double
        public private(set) var isJumping = false

        private let groundY: Double
        private let initialSpeed: Double
        private let initialFrame: Double
        private var speed: Double
        private var frame: Double

        public init(width: Double, height: Double, x: Double, groundY: Double) {
            self.width = width
            self.height = height
            self.x = x
            self.y = groundY
            self.groundY = groundY
            self.initialSpeed = Constants.playerSpeed
            self.initialFrame = 0.2
            self.speed = Constants.playerSpeed
            self.frame = 0.2
        }

        /// 한 틱 이동. 점프 중이 아니면 아무 일도 없다.
        public mutating func move() {
            guard isJumping else { return }

            speed -= Constants.gravity * frame
            // y 가 아래로 크므로 위로 갈수록 빼기다.
            y = min(y - speed * frame, groundY)

            if y == groundY {
                isJumping = false
                speed = 0
                frame = 0
            }
        }

        /// 점프. **이미 점프 중이면 무시한다** — 연타로 날아오르지 않게.
        public mutating func jump() {
            guard !isJumping else { return }
            isJumping = true
            y = groundY
            speed = initialSpeed
            frame = initialFrame
        }
    }

    // MARK: - 장애물

    public struct Hurdle: Sendable, Equatable, Identifiable {
        public let id = UUID()
        public let width: Double = 40
        public let height: Double = 30
        public private(set) var x: Double
        public let y: Double
        public private(set) var didScore = false

        let speed: Double

        init(x: Double, y: Double, speed: Double) {
            self.x = x
            self.y = y
            self.speed = speed
        }

        mutating func move() { x -= speed }
        mutating func markScored() { didScore = true }

        public static func == (lhs: Hurdle, rhs: Hurdle) -> Bool { lhs.id == rhs.id }
    }

    // MARK: - 상태

    public private(set) var player: Player
    public private(set) var hurdles: [Hurdle] = []
    public private(set) var score = 0
    public private(set) var elapsedMillis = 0
    public private(set) var isRunning = false

    private let groundY: Double
    private let startX: Double
    private let endX: Double
    /// 지금까지 만든 장애물 수. 생성 주기와 속도 증가에 함께 쓴다.
    private var hurdleCount = 0

    public init(
        playerWidth: Double, playerHeight: Double, playerX: Double,
        groundY: Double, startX: Double, endX: Double
    ) {
        self.player = Player(width: playerWidth, height: playerHeight, x: playerX, groundY: groundY)
        self.groundY = groundY
        self.startX = startX
        self.endX = endX
    }

    /// 한 틱의 길이(밀리초).
    ///
    /// ⚠️ **정수 나눗셈이다** — `1000 / 60 == 16`, 16.67 이 아니다.
    /// 게임 시계가 벽시계보다 4% 느리게 간다는 뜻이고, 장애물 생성 주기도 그만큼 늦다.
    /// Android 도 `1000L / FRAME` 으로 같은 값이라 **동작은 일치한다.** 고치면 난이도가 달라진다.
    public static let tickMillis: Int = 1000 / Constants.frame
    public static var tickDuration: Duration { .milliseconds(tickMillis) }

    public mutating func start() { isRunning = true }
    public mutating func stop() { isRunning = false }
    public mutating func jump() { player.jump() }

    /// 한 프레임 진행. 원본 `RunnerEngine.start()` 루프 한 바퀴와 같은 순서다:
    /// 시간 → 이동 → 충돌 → 득점 → 정리 → 생성.
    ///
    /// - Returns: 충돌해서 게임이 끝났으면 true
    @discardableResult
    public mutating func tick() -> Bool {
        guard isRunning else {
            // 게임이 끝나도 점프 중이면 착지까지는 그린다 (원본의 두 번째 while).
            player.move()
            return false
        }

        elapsedMillis += Self.tickMillis

        player.move()
        for index in hurdles.indices { hurdles[index].move() }

        if hasCollision() {
            isRunning = false
            return true
        }

        // 플레이어 아래를 지난 장애물은 한 번만 점수가 된다.
        for index in hurdles.indices where !hurdles[index].didScore {
            if isUnderPlayer(hurdles[index]) {
                hurdles[index].markScored()
                score += 1
            }
        }

        hurdles.removeAll { $0.x < startX }

        // 5개마다 조금씩 빨라진다 (원본 `totalHurdleCount / 5 * 0.125f`).
        if elapsedMillis / Constants.hurdleGenDelayMillis > hurdleCount {
            let speed = Constants.hurdleSpeed + Double(hurdleCount / 5) * 0.125
            hurdles.append(Hurdle(x: endX, y: groundY, speed: speed))
            hurdleCount = elapsedMillis / Constants.hurdleGenDelayMillis
        }

        return false
    }

    // MARK: - 판정

    /// 축 정렬 사각형끼리라 겹침만 보면 된다.
    ///
    /// 원본은 SAT(분리축 정리)로 일반 다각형처럼 풀지만, 두 도형 모두 회전하지 않는
    /// 사각형이라 결과가 같다. 계산을 줄이고 읽기 쉬운 쪽을 택했다 —
    /// **판정 결과는 원본과 동일하다** (`RunnerEngineTests` 가 고정한다).
    private func hasCollision() -> Bool {
        hurdles.contains { overlaps($0) }
    }

    private func overlaps(_ hurdle: Hurdle) -> Bool {
        let p = Constants.collisionPadding
        let playerMinX = player.x + p
        let playerMaxX = player.x - p + player.width
        let playerMinY = player.y + p
        let playerMaxY = player.y - p + player.height

        let hurdleMinX = hurdle.x + p
        let hurdleMaxX = hurdle.x - p + hurdle.width
        let hurdleMinY = hurdle.y + p
        let hurdleMaxY = hurdle.y - p + hurdle.height

        return playerMaxX >= hurdleMinX && hurdleMaxX >= playerMinX
            && playerMaxY >= hurdleMinY && hurdleMaxY >= playerMinY
    }

    /// 가로 범위가 겹치면 "아래를 지나는 중"이다. 원본과 같은 판정이다.
    private func isUnderPlayer(_ hurdle: Hurdle) -> Bool {
        let p = Constants.collisionPadding
        let playerMinX = player.x + p
        let playerMaxX = player.x - p + player.width
        let hurdleMinX = hurdle.x + p
        let hurdleMaxX = hurdle.x - p + hurdle.width

        return (hurdleMinX >= playerMinX && hurdleMinX <= playerMaxX)
            || (hurdleMaxX >= playerMinX && hurdleMaxX <= playerMaxX)
    }
}

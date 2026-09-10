import Foundation

/// 농구 훈련 엔진
///
/// Android `presentation/.../training/basketball/engine/{BasketballEngine,Basketball,Ball,Basket}.kt`
/// (386 LOC) 이식. 달리기와 같은 이유로 `MongsModel` 에 둔다 — 순수 계산이고 테스트가 필요하다.
///
/// 좌표계는 화면과 같다 (**y 는 아래로 증가**). 공을 위로 던지면 `py` 가 줄어든다.
public struct BasketballEngine: Sendable {

    public enum Constants {
        public static let gravity: Double = 9.8
        public static let frame: Int = 60
        public static let ballSpeed: Double = 80
        /// 골대에 부딪혔을 때의 반발 계수. 1보다 커서 **튕기면 더 빨라진다** —
        /// 물리적으로는 이상하지만 원본 값이고, 낮추면 공이 림에 붙어버린다.
        public static let tension: Double = 1.18
        /// 이 아래로 떨어지면 공을 처음 자리로 되돌린다.
        public static let resetY: Double = 600
    }

    // MARK: - 골대

    public struct Basket: Sendable, Equatable {
        public let width: Double
        public let height: Double
        public let x: Double
        public let y: Double

        /// 림 양 끝점의 충돌 반지름
        public var radius: Double { height / 2 }
        /// 왼쪽 림
        public var leftX: Double { x - width / 2 }
        /// 오른쪽 림
        public var rightX: Double { x + width / 2 }

        public init(width: Double, height: Double, x: Double, y: Double) {
            self.width = width
            self.height = height
            self.x = x
            self.y = y
        }
    }

    // MARK: - 공

    public struct Ball: Sendable, Equatable {
        public private(set) var x: Double
        public private(set) var y: Double
        /// 멀어질수록 작아진다 — 원근 흉내다.
        public private(set) var radius: Double
        /// 굴러가는 회전각(도)
        public private(set) var degree: Double = 0
        public private(set) var isThrown = false
        /// 림 위로 올라갔는지. **여기부터 충돌·골 판정이 켜진다.**
        public private(set) var isAboveRim = false
        public private(set) var didGoal = false

        private let initialX: Double
        private let initialY: Double
        private let minRadius: Double
        private let maxRadius: Double
        private var speedX: Double = 0
        private var speedY: Double = 0
        private var frame: Double = 0

        public init(x: Double, y: Double, minRadius: Double, maxRadius: Double) {
            self.x = x
            self.y = y
            self.initialX = x
            self.initialY = y
            self.minRadius = minRadius
            self.maxRadius = maxRadius
            self.radius = maxRadius
        }

        mutating func move() {
            guard isThrown else { return }

            speedY -= Constants.gravity * frame
            y -= speedY * frame
            x -= speedX * frame

            radius = max(radius - 0.45, minRadius)

            if speedX < 0 {
                degree += 2
            } else if speedX > 0 {
                degree -= 2
            }
            if degree >= 360 { degree = 0 } else if degree < 0 { degree = 360 }

            if y > Constants.resetY { reset() }
        }

        private mutating func reset() {
            x = initialX
            y = initialY
            degree = 0
            radius = maxRadius
            speedX = 0
            speedY = 0
            frame = 0
            isThrown = false
            isAboveRim = false
            didGoal = false
        }

        /// 던진다. `(vx, vy)` 는 **끌어당긴 끝점**이고, 시작점과의 차이가 방향이 된다.
        ///
        /// 방향 벡터를 정규화하지 않고 **큰 쪽 성분을 `ballSpeed` 로 고정**한 뒤
        /// 나머지를 비례로 맞춘다 (원본 그대로). 대각선이 축 방향보다 빨라지지만
        /// 그게 원본의 손맛이다.
        mutating func throwBall(vx: Double, vy: Double) {
            guard !isThrown else { return }
            isThrown = true
            x = initialX
            y = initialY
            frame = 0.2

            speedY = initialY - vy
            speedX = initialX - vx

            if speedY > speedX {
                speedX = Constants.ballSpeed * (speedX / speedY)
                speedY = Constants.ballSpeed
            } else if speedY == speedX {
                speedY = Constants.ballSpeed
                speedX = Constants.ballSpeed
            } else {
                speedY = Constants.ballSpeed * (speedY / speedX)
                speedX = Constants.ballSpeed
            }
        }

        mutating func markAboveRim() { isAboveRim = true }
        mutating func markGoal() { didGoal = true }

        /// 림 끝점과 부딪혔을 때의 탄성 충돌.
        ///
        /// 충돌면 법선 방향 성분만 뒤집고 `tension` 을 곱한다. 접선 성분은 그대로 둔다.
        mutating func bounce(offX: Double, offY: Double) {
            let dx = x - offX
            let dy = y - offY
            let distance = (dx * dx + dy * dy).squareRoot()
            guard distance > 0 else { return }

            let cos = dx / distance
            let sin = dy / distance

            let normal = speedX * cos + speedY * sin
            let tangent = -speedX * sin + speedY * cos
            let bounced = -Constants.tension * normal

            speedX = bounced * cos + tangent * (-sin)
            speedY = bounced * sin + tangent * cos
        }
    }

    // MARK: - 상태

    public private(set) var ball: Ball
    public let basket: Basket
    public private(set) var score = 0
    public private(set) var elapsedMillis = 0
    public private(set) var isRunning = false

    public static let tickMillis: Int = 1000 / Constants.frame
    public static var tickDuration: Duration { .milliseconds(tickMillis) }

    public init(ball: Ball, basket: Basket) {
        self.ball = ball
        self.basket = basket
    }

    public mutating func start() { isRunning = true }
    public mutating func stop() { isRunning = false }

    public mutating func throwBall(vx: Double, vy: Double) {
        ball.throwBall(vx: vx, vy: vy)
    }

    /// 한 프레임 진행. 원본 루프와 같은 순서다: 이동 → (림 위면) 충돌 → 골.
    public mutating func tick() {
        guard isRunning || ball.isThrown else { return }
        if isRunning { elapsedMillis += Self.tickMillis }

        ball.move()

        // 공 아랫면이 림 윗면보다 위로 올라가면 그때부터 판정을 켠다.
        if ball.y + ball.radius < basket.y - basket.height / 2 {
            ball.markAboveRim()
        }

        guard ball.isAboveRim else { return }

        if let point = collisionPoint() {
            ball.bounce(offX: point.x, offY: point.y)
        }

        if !ball.didGoal, isInsideBasket() {
            ball.markGoal()
            score += 1
        }
    }

    // MARK: - 판정

    /// 림 왼쪽 끝 → 오른쪽 끝 순서로 본다 (원본과 같은 우선순위).
    private func collisionPoint() -> (x: Double, y: Double)? {
        let sum = ball.radius + basket.radius

        let left = distance(from: (basket.leftX, basket.y))
        if left <= sum { return (basket.leftX, basket.y) }

        let right = distance(from: (basket.rightX, basket.y))
        if right <= sum { return (basket.rightX, basket.y) }

        return nil
    }

    private func distance(from point: (x: Double, y: Double)) -> Double {
        let dx = ball.x - point.x
        let dy = ball.y - point.y
        return (dx * dx + dy * dy).squareRoot()
    }

    /// 림 사각형 안에 들어왔는지. `isAboveRim` 이 켜진 뒤에만 보므로
    /// **위에서 내려와 통과한 경우**만 골이 된다.
    private func isInsideBasket() -> Bool {
        ball.y <= basket.y + basket.height / 2
            && ball.y >= basket.y - basket.height / 2
            && ball.x <= basket.x + basket.width / 2
            && ball.x >= basket.x - basket.width / 2
    }
}

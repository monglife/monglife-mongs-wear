package com.mongs.wear.presentation.pages.training.basketball

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.pow
import kotlin.math.sqrt

class BasketballEngine {

    companion object {
        private const val TAG = "BasketballEngine"

        private const val GRAVITY = 9.8f
        private const val FRAME = 60L

        // 공 기본 속도
        private const val BALL_SPEED = 80f

        private const val TENSION = 1.18f
    }

    private val _endEvent = MutableSharedFlow<Long>()
    val endEvent = _endEvent.asSharedFlow()

    val playMillis: MutableState<Long> = mutableLongStateOf(0L)
    val isStartGame: MutableState<Boolean> = mutableStateOf(false)
    val score: MutableState<Int> = mutableIntStateOf(0)
    val ball: MutableState<Ball?> = mutableStateOf(null)
    val basket: MutableState<Basket?> = mutableStateOf(null)

    /**
     * 게임 시작
     */
    fun start(ballSy: Float, ballSx: Float, ballSr: Float, basketSy: Float, basketSx: Float, basketWidth: Float, basketHeight: Float) {

        isStartGame.value = true
        playMillis.value = 0L
        score.value = 0

        ball.value = Ball(
            initSpeed = BALL_SPEED,
            initX = ballSx,
            initY = ballSy,
            initRadius = ballSr,
            initDegrees = 0f,
            initFrame = 0.2f,
        )

        basket.value = Basket(
            initX = basketSx,
            initY = basketSy,
            width = basketWidth,
            height = basketHeight,
        )

        CoroutineScope(Dispatchers.IO).launch {
            while (isStartGame.value) {
                val processStart = LocalDateTime.now()

                // 게임 시간 증가
                playMillis.value += 1000L / FRAME

                ball.value?.let { ball ->
                    basket.value?.let { basket ->
                        // 농구공 이동
                        moveBall(ball = ball, basket = basket)

                        if (ball.isTop.value) {
                            // 왼쪽 충돌 확인
                            if (isCollision(
                                    ballRadius = ball.radius.value,
                                    ballX = ball.currentX.value,
                                    ballY = ball.currentY.value,
                                    basketRadius = basket.radius,
                                    basketX = basket.leftX.value,
                                    basketY = basket.leftY.value,
                                )
                            ) {
                                ball.isCollisionFirst()

                                val sin = getSin(ballX = ball.currentX.value, ballY = ball.currentY.value, basketX = basket.leftX.value, basketY = basket.leftY.value)
                                val cos = getCos(ballX = ball.currentX.value, ballY = ball.currentY.value, basketX = basket.leftX.value, basketY = basket.leftY.value)

                                ball.updateSpeed(sin = sin, cos = cos, e = TENSION)
                            }

                            // 오른쪽 충돌 확인
                            if (isCollision(
                                    ballRadius = ball.radius.value,
                                    ballX = ball.currentX.value,
                                    ballY = ball.currentY.value,
                                    basketRadius = basket.radius,
                                    basketX = basket.rightX.value,
                                    basketY = basket.rightY.value,
                                )
                            ) {
                                ball.isCollisionFirst()

                                val sin = getSin(ballX = ball.currentX.value, ballY = ball.currentY.value, basketX = basket.rightX.value, basketY = basket.rightY.value)
                                val cos = getCos(ballX = ball.currentX.value, ballY = ball.currentY.value, basketX = basket.rightX.value, basketY = basket.rightY.value)

                                ball.updateSpeed(sin = sin, cos = cos, e = TENSION)
                            }

                            // 골 판별
                            if (!ball.isGoal.value && isGoal(ball = ball, basket = basket)) {
                                ball.isGoal()
                                score.value += 1
                            }
                        }
                    }
                }

                // 좌표 변경 로직 시간 측정
                val processMillis = Duration.between(processStart, LocalDateTime.now()).toMillis()

                // 좌표 변경 로직 시간 제외한 딜레이 진행
                delay(0L.coerceAtLeast(1000L / FRAME - processMillis))
            }

            ball.value?.let { ball ->
                while (ball.isThrowing.value) {
                    val processStart = LocalDateTime.now()

                    // 게임 시간 증가
                    playMillis.value += 1000L / FRAME

                    // 농구공 이동
                    basket.value?.let { basket ->
                        moveBall(ball = ball, basket = basket)
                    }

                    // 좌표 변경 로직 시간 측정
                    val processMillis = Duration.between(processStart, LocalDateTime.now()).toMillis()

                    // 좌표 변경 로직 시간 제외한 딜레이 진행
                    delay(0L.coerceAtLeast(1000L / FRAME - processMillis))
                }
            }

            _endEvent.emit(System.currentTimeMillis())
        }
    }

    /**
     * 게임 종료
     */
    fun end() {
        isStartGame.value = false
    }

    /**
     * 농구공 이동
     */
    private fun moveBall(ball: Ball, basket: Basket) {

        ball.move()

        val ballBottomY = ball.currentY.value + ball.radius.value
        val basketTopY = basket.currentY.value - basket.height / 2

        if (ballBottomY < basketTopY) {
            ball.isTopPosition()
        }
    }

    /**
     * 공과 농구 골대의 충돌 여부 확인
     */
    private fun isCollision(ballRadius: Float, ballX: Float, ballY: Float, basketRadius: Float, basketX: Float, basketY: Float): Boolean {
        val radiusSum = ballRadius + basketRadius
        val distance = sqrt((ballX - basketX).pow(2) + (ballY - basketY).pow(2))

        return distance <= radiusSum
    }

    /**
     * 골 판별
     */
    private fun isGoal(ball: Ball, basket: Basket) =
        ball.currentY.value <= basket.currentY.value + basket.height / 2 &&
        ball.currentY.value >= basket.currentY.value - basket.height / 2 &&
        ball.currentX.value <= basket.currentX.value + basket.width / 2 &&
        ball.currentX.value >= basket.currentX.value - basket.width / 2

    /**
     * sin 값 계산
     */
    private fun getSin(ballX: Float, ballY: Float, basketX: Float, basketY: Float): Float {

        val distance = sqrt((ballX - basketX).pow(2) + (ballY - basketY).pow(2))

        return (ballY - basketY) / distance
    }

    /**
     * cos 값 계산
     */
    private fun getCos(ballX: Float, ballY: Float, basketX: Float, basketY: Float): Float {

        val distance = sqrt((ballX - basketX).pow(2) + (ballY - basketY).pow(2))

        return (ballX - basketX) / distance
    }

    /**
     * 농구공 클래스
     */
    class Ball(
        private val initX: Float,                   // 초기 지점 X
        private val initY: Float,                   // 초기 지점 Y
        private val initSpeed: Float,               // 초기 이동 속도
        private val initFrame: Float,               // 초기 프레임 (중력 보정 값)
        private val initRadius: Float,              // 초기 반지름 (원)
        private val initDegrees: Float,             // 초기 회전 각도 (공 이미지)
        private var throwSpeedX: Float = 0f,        // 현재 X 축 이동 속도
        private var throwSpeedY: Float = 0f,        // 현재 Y 축 이동 속도
        private var throwFrame: Float = initFrame,  // 보정 값
        var degrees: Float = initDegrees,
        var radius: MutableState<Float> = mutableFloatStateOf(initRadius),
        var currentX: MutableState<Float> = mutableFloatStateOf(initX),
        var currentY: MutableState<Float> = mutableFloatStateOf(initY),
        var isThrowing: MutableState<Boolean> = mutableStateOf(false),
        var isCollision: MutableState<Boolean> = mutableStateOf(false),
        var isTop: MutableState<Boolean> = mutableStateOf(false),
        var isGoal: MutableState<Boolean> = mutableStateOf(false),
    ) {
        /**
         * 던짐
         */
        fun throwing(vy: Float, vx: Float) {
            if (!isThrowing.value) {
                this.isThrowing.value = true
                this.currentY.value = initY
                this.currentX.value = initX
                this.throwSpeedY = initY - vy
                this.throwSpeedX = initX - vx
                this.throwFrame = initFrame

                if (this.throwSpeedY > this.throwSpeedX) {
                    this.throwSpeedX = initSpeed * (this.throwSpeedX / this.throwSpeedY)
                    this.throwSpeedY = initSpeed
                } else if (this.throwSpeedY == this.throwSpeedX) {
                    this.throwSpeedY = initSpeed
                    this.throwSpeedX = initSpeed
                } else {
                    this.throwSpeedY = initSpeed * (this.throwSpeedY / this.throwSpeedX)
                    this.throwSpeedX = initSpeed
                }
            }
        }

        /**
         * 이동
         */
        fun move() {
            if (isThrowing.value) {
                // Y 축 중력 적용
                this.throwSpeedY -= GRAVITY * this.throwFrame

                // 좌표 변경
                val nextPy = this.currentY.value - this.throwSpeedY  * this.throwFrame
                val nextPx = this.currentX.value - this.throwSpeedX  * this.throwFrame
                this.currentY.value = nextPy
                this.currentX.value = nextPx

                if (!this.isCollision.value && !this.isGoal.value) {
                    this.radius.value -= 0.3f
                }

                // 각도 변경
                if (this.throwSpeedX < 0f) {
                    this.degrees += 2f
                } else if (this.throwSpeedX > 0f) {
                    this.degrees -= 2f
                }

                if (this.degrees >= 360f) {
                    this.degrees = 0f
                } else if (this.degrees < 0f) {
                    this.degrees = 360f
                }

                // 값 초기화
                if (this.currentY.value > 600f) {
                    this.isThrowing.value = false
                    this.currentY.value = initY
                    this.currentX.value = initX
                    this.degrees = initDegrees
                    this.radius.value = initRadius
                    this.throwSpeedY = 0f
                    this.throwSpeedX = 0f
                    this.throwFrame = 0f
                    this.isCollision.value = false
                    this.isTop.value = false
                    this.isGoal.value = false
                }
            }
        }

        fun isCollisionFirst() {
            this.isCollision.value = true
        }

        /**
         * 골대 상단 위치 여부
         */
        fun isTopPosition() {
            this.isTop.value = true
        }

        /**
         * 골 여부
         */
        fun isGoal() {
            this.isGoal.value = true
        }

        /**
         * 충돌 후 방향 업데이트
         */
        fun updateSpeed(sin: Float, cos: Float, e: Float) {
            val vn = throwSpeedX * cos + throwSpeedY * sin      // 충돌 방향 속도
            val vt = -throwSpeedX * sin + throwSpeedY * cos     // 수직 방향 속도 (변하지 않음)

            // 탄성 충돌 공식 적용
            val newVn = -e * vn

            // 새로운 속도를 x, y 축으로 변환
            throwSpeedX = newVn * cos + vt * (-sin)
            throwSpeedY = newVn * sin + vt * cos
        }
    }

    /**
     * 농구 골대
     */
    class Basket(
        private val initX: Float,
        private val initY: Float,
        val width: Float,
        val height: Float,
        val radius: Float = height / 2,
        var currentX: MutableState<Float> = mutableFloatStateOf(initX),
        var currentY: MutableState<Float> = mutableFloatStateOf(initY),
        var leftX: MutableState<Float> = mutableFloatStateOf(currentX.value - width / 2),
        var leftY: MutableState<Float> = mutableFloatStateOf(currentY.value),
        var rightX: MutableState<Float> = mutableFloatStateOf(currentX.value + width / 2),
        var rightY: MutableState<Float> = mutableFloatStateOf(currentY.value),
    )
}
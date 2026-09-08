package com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine

import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.vo.BasketballVo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BasketballEngine @Inject constructor() {
    companion object {
        private const val GRAVITY = 9.8f
        private const val FRAME = 60L
        private const val BALL_SPEED = 80f
        private const val TENSION = 1.18f
    }

    private val processMap = ConcurrentHashMap<String, Basketball>()

    /**
     * 생성
     */
    fun generate(
        ballInitY: Float,
        ballInitX: Float,
        ballInitRadius: Float,
        basketHeight: Float,
        basketWidth: Float,
        basketTopInitY: Float,
        basketTopInitX: Float,
        ratio: Float,
    ): BasketballVo {

        val basket = Basket(
            height = basketHeight * ratio,
            width = basketWidth * ratio,
            topInitY = basketTopInitY,
            topInitX = basketTopInitX,
        )
        val ball = Ball(
            gravity = GRAVITY,
            tension = TENSION,
            initY = ballInitY,
            initX = ballInitX,
            initSpeed = BALL_SPEED,
            initFrame = 0.2f,
            initMinRadius = ballInitRadius * ratio,
            initMaxRadius = ballInitRadius,
        )
        val basketball = Basketball(
            isStart = false,
            isProcess = false,
            timeMillis = 0L,
            score = 0,
            basket = basket,
            ball = ball,
        )

        processMap[basketball.basketballId] = basketball

        return BasketballVo.of(basketball)
    }

    /**
     * 시작
     */
    fun start(basketballId: String) = flow {
        processMap[basketballId]?.let {
            it.start()

            while(it.isProcess) {

                val cycleMillis = 1000L / FRAME
                val cycleStart = LocalDateTime.now()

                it.increaseTimeMillis(cycleMillis)

                // 물체 이동
                it.moveBall()

                if (it.ball.isTop) {
                    // 충돌 시 각속도 업데이트
                    it.isCollision()?.let { basketPoint ->
                        it.ball.updateSpeed(
                            ty = basketPoint.y,
                            tx = basketPoint.x,
                        )
                    }

                    // 골 여부 판별
                    if (!it.ball.isGoal && it.isGoal()) {
                        it.ball.goal()
                        it.increaseScore()
                    }
                }

                delay(0L.coerceAtLeast(cycleMillis - Duration.between(cycleStart, LocalDateTime.now()).toMillis()))

                emit(BasketballVo.of(it))
            }

            // 게임 끝
            while (it.ball.isThrow) {

                val cycleMillis = 1000L / FRAME
                val cycleStart = LocalDateTime.now()

                // 물체 이동
                it.moveBall()

                delay(0L.coerceAtLeast(cycleMillis - Duration.between(cycleStart, LocalDateTime.now()).toMillis()))

                emit(BasketballVo.of(it))
            }
        }
    }

    /**
     * 종료
     */
    fun stop(basketballId: String) {
        processMap[basketballId]?.let {
            it.stop()
            processMap.remove(it.basketballId)
        }
    }

    /**
     * 공 던지기
     */
    fun throwBall(basketballId: String, vy: Float, vx: Float) {
        processMap[basketballId]?.ball?.throwing(vy = vy, vx = vx)
    }
}
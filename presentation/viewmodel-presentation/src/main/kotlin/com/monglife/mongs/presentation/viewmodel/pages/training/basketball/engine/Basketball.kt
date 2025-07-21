package com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine

import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

open class Basketball(
    val basketballId: String = UUID.randomUUID().toString(),
    var isProcess: Boolean,
    var timeMillis: Long,
    var score: Int,
    val basket: Basket,
    val ball: Ball,
) {

    /**
     * 시작
     */
    fun start() {
        this.isProcess = true
    }

    /**
     * 종료
     */
    fun stop() {
        this.isProcess = false
    }

    /**
     * 게임 시간 동기화
     */
    fun increaseTimeMillis(timeMillis: Long) {
        this.timeMillis += timeMillis
    }

    /**
     * 공 이동
     */
    fun moveBall() {
        ball.move()

        val ballBottomY = ball.py + ball.radius
        val basketTopY = basket.py - basket.height / 2

        if (ballBottomY < basketTopY) {
            ball.topPosition()
        }
    }

    /**
     * 스코어 증가
     */
    fun increaseScore() {
        this.score += 1;
    }

    /**
     * 공과 농구 골대의 충돌 여부 확인
     */
    fun isCollision(): Point? {
        val radiusSum = ball.radius + basket.radius
        val leftDistance = sqrt((ball.px - basket.lx).pow(2) + (ball.py - basket.ly).pow(2))
        val rightDistance = sqrt((ball.px - basket.rx).pow(2) + (ball.py - basket.ry).pow(2))

        if (leftDistance <= radiusSum) {
            return Point(
                y = basket.ly,
                x = basket.lx,
            )
        } else if(rightDistance <= radiusSum) (
            return Point(
                y = basket.ry,
                x = basket.rx,
            )
        )

        return null
    }

    /**
     * 골 판별
     */
    fun isGoal() =
        ball.py <= basket.py + basket.height / 2 &&
        ball.py >= basket.py - basket.height / 2 &&
        ball.px <= basket.px + basket.width  / 2 &&
        ball.px >= basket.px - basket.width  / 2

    data class Point(val x: Float, val y: Float)
}

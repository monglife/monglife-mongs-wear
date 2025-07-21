package com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

data class Ball(
    private val gravity: Float,
    private val tension: Float,
    private val initY: Float,
    private val initX: Float,
    private val initSpeed: Float,
    private val initFrame: Float,
    private val initMinRadius: Float,
    private val initMaxRadius: Float,
    var py: Float = initY,
    var px: Float = initX,
    private var speedY: Float = 0f,
    private var speedX: Float = 0f,
    private var frame: Float = 0f,
    var radius: Float = initMaxRadius,
    var degree: Float = 0f,
    var isThrow: Boolean = false,
    var isTop: Boolean = false,
    var isGoal: Boolean = false,
) {

    /**
     * 이동
     */
    fun move() {
        if (isThrow) {
            // Y 축 중력 적용
            this.speedY -= gravity * this.frame

            // 좌표 변경
            val nextPy = this.py - this.speedY  * this.frame
            val nextPx = this.px - this.speedX  * this.frame
            this.py = nextPy
            this.px = nextPx

            // 원 반지름 감소
            this.radius = max(this.radius - 0.5f, initMinRadius)

            // 각도 변경
            if (this.speedX < 0f) {
                this.degree += 2f
            } else if (this.speedX > 0f) {
                this.degree -= 2f
            }

            if (this.degree >= 360f) {
                this.degree = 0f
            } else if (this.degree < 0f) {
                this.degree = 360f
            }

            // 값 초기화
            if (this.py > 600f) {
                this.py = initY
                this.px = initX
                this.degree = 0f
                this.radius = initMaxRadius
                this.speedY = 0f
                this.speedX = 0f
                this.frame = 0f
                this.isThrow = false
                this.isTop = false
                this.isGoal = false
            }
        }
    }

    /**
     * 던짐
     */
    fun throwing(vy: Float, vx: Float) {
        if (!isThrow) {
            this.isThrow = true
            this.py = initY
            this.px = initX
            this.speedY = initY - vy
            this.speedX = initX - vx
            this.frame = initFrame

            if (this.speedY > this.speedX) {
                this.speedX = initSpeed * (this.speedX / this.speedY)
                this.speedY = initSpeed
            } else if (this.speedY == this.speedX) {
                this.speedY = initSpeed
                this.speedX = initSpeed
            } else {
                this.speedY = initSpeed * (this.speedY / this.speedX)
                this.speedX = initSpeed
            }
        }
    }

    /**
     * 골대 상단 위치 여부
     */
    fun topPosition() {
        this.isTop = true
    }

    /**
     * 골 여부
     */
    fun goal() {
        this.isGoal = true
    }

    /**
     * 충돌 후 방향 업데이트
     */
    fun updateSpeed(ty: Float, tx: Float) {
        val vn = speedX * getCos(ty, tx) + speedY * getSin(ty, tx)      // 충돌 방향 속도
        val vt = -speedX * getSin(ty, tx) + speedY * getCos(ty, tx)     // 수직 방향 속도 (변하지 않음)

        // 탄성 충돌 공식 적용
        val newVn = -tension * vn

        // 새로운 속도를 x, y 축으로 변환
        speedX = newVn * getCos(ty, tx) + vt * (-getSin(ty, tx))
        speedY = newVn * getSin(ty, tx) + vt * getCos(ty, tx)
    }

    /**
     * sin 값 계산
     */
    private fun getSin(ty: Float, tx: Float): Float {
        val distance = sqrt((px - tx).pow(2) + (py - ty).pow(2))
        return (py - ty) / distance
    }

    /**
     * cos 값 계산
     */
    private fun getCos(ty: Float, tx: Float): Float {
        val distance = sqrt((px - tx).pow(2) + (py - ty).pow(2))
        return (px - tx) / distance
    }
}

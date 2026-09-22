package com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine

import kotlin.math.min

data class RunnerPlayer(
    val height: Int,
    val width: Int,
    private val gravity: Float,
    private val initY: Float,
    private val initX: Float,
    private val initSpeed: Float,
    private val initFrame: Float,
    var py: Float = initY,
    var px: Float = initX,
    private var speed: Float = initSpeed,
    private var frame: Float = initFrame,
    var isJump: Boolean = false,
) {

    /**
     * 이동
     */
    fun move() {
        if (isJump) {
            // 스피드 변경
            this.speed -= this.gravity * this.frame

            // 좌표 변경
            val nextPy = this.py - this.speed * this.frame
            this.py = min(nextPy, this.initY)

            // 지면 도착 여부 확인
            if (this.py == this.initY) {
                this.isJump = false
                this.speed = 0f
                this.frame = 0f
            }
        }
    }

    /**
     * 점프
     */
    fun jump() {
        if (!isJump) {
            this.isJump = true
            this.py = this.initY
            this.speed = this.initSpeed
            this.frame = this.initFrame
        }
    }
}
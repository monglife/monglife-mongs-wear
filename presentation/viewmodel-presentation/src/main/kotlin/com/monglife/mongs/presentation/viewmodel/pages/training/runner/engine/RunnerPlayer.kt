package com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine

import kotlin.math.min

data class RunnerPlayer(
    val height: Int,
    val width: Int,
    private val gravity: Float,
    private val startY: Float,
    private val startX: Float,
    private val startSpeed: Float,
    private val startFrame: Float,
    var py: Float = startY,
    var px: Float = startX,
    var speed: Float = startSpeed,
    var frame: Float = startFrame,
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
            this.py = min(nextPy, this.startY)

            // 지면 도착 여부 확인
            if (this.py == this.startY) {
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
            this.py = this.startY
            this.speed = this.startSpeed
            this.frame = this.startFrame
        }

    }
}
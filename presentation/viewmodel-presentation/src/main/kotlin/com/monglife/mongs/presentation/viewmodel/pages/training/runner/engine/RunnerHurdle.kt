package com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine

data class RunnerHurdle(
    val height: Int,
    val width: Int,
    private val initY: Float,
    private val initX: Float,
    var py: Float = initY,
    var px: Float = initX,
    private val speed: Float,
    var isContainedScore: Boolean = false,
) {

    /**
     * 이동
     */
    fun move() {
        this.px -= this.speed
    }

    /**
     * 스코어 득점 여부 확인
     */
    fun checkContainedScore() {
        isContainedScore = true
    }
}
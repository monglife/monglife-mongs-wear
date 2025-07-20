package com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine

data class RunnerHurdle(
    val height: Int,
    val width: Int,
    private val startY: Float,
    private val startX: Float,
    private val startSpeed: Float,
    var py: Float = startY,
    var px: Float = startX,
    private var speed: Float = startSpeed,
    var isContainedScore: Boolean = false,
) {

    /**
     * 이동
     */
    fun move() {
        this.px -= this.speed
    }
}
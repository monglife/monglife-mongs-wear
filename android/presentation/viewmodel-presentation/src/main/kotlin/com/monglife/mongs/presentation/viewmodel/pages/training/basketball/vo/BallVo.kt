package com.monglife.mongs.presentation.viewmodel.pages.training.basketball.vo

import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine.Ball

data class BallVo(
    val py: Float,
    val px: Float,
    val radius: Float,
    val degree: Float,
    val isTop: Boolean,
) {
    companion object {
        fun of(ball: Ball) = BallVo(
            py = ball.py,
            px = ball.px,
            radius = ball.radius,
            degree = ball.degree,
            isTop = ball.isTop,
        )
    }
}

package com.monglife.mongs.presentation.viewmodel.pages.training.basketball.vo

import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine.Basketball

data class BasketballVo(
    val basketballId: String,
    val isProcess: Boolean,
    val timeMillis: Long,
    val score: Int,
    val basketVo: BasketVo,
    val ballVo: BallVo,
) {
    companion object {
        fun of(basketball: Basketball) = BasketballVo(
            basketballId = basketball.basketballId,
            isProcess = basketball.isProcess,
            timeMillis = basketball.timeMillis,
            score = basketball.score,
            basketVo = BasketVo.of(basketball.basket),
            ballVo = BallVo.of(basketball.ball),
        )
    }
}
package com.monglife.mongs.presentation.viewmodel.pages.training.runner.vo

import com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine.RunnerPlayer

data class RunnerPlayerVo(
    val height: Int,
    val width: Int,
    var py: Float,
    var px: Float,
) {
    companion object {
        fun of(runnerPlayer: RunnerPlayer) = RunnerPlayerVo(
            height = runnerPlayer.height,
            width = runnerPlayer.width,
            py = runnerPlayer.py,
            px = runnerPlayer.px,
        )
    }
}

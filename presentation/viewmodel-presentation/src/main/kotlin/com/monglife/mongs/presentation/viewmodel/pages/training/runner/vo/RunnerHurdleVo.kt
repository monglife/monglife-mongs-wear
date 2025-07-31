package com.monglife.mongs.presentation.viewmodel.pages.training.runner.vo

import com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine.RunnerHurdle

data class RunnerHurdleVo(
    val height: Int,
    val width: Int,
    var py: Float,
    var px: Float,
) {
    companion object {
        fun of(runnerHurdle: RunnerHurdle) = RunnerHurdleVo(
            height = runnerHurdle.height,
            width = runnerHurdle.width,
            py = runnerHurdle.py,
            px = runnerHurdle.px,
        )
    }
}

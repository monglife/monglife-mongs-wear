package com.monglife.mongs.presentation.viewmodel.pages.training.runner.vo

import com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine.Runner

data class RunnerVo(
    val runnerId: String,
    val isProcess: Boolean,
    val timeMillis: Long,
    val score: Int,
    val runnerHurdleVos: List<RunnerHurdleVo>,
    val runnerPlayerVo: RunnerPlayerVo,
) {
    companion object {
        fun of(runner: Runner) = RunnerVo(
            runnerId = runner.runnerId,
            isProcess = runner.isProcess,
            timeMillis = runner.timeMillis,
            score = runner.score,
            runnerHurdleVos = runner.runnerHurdles.map { RunnerHurdleVo.of(it) },
            runnerPlayerVo = RunnerPlayerVo.of(runner.runnerPlayer),
        )
    }
}

package com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine

import java.util.UUID

data class Runner(
    val runnerId: String = UUID.randomUUID().toString(),
    var isProcess: Boolean,
    val score: Int,
    val runnerHurdles: List<RunnerHurdle>,
    val runnerPlayer: RunnerPlayer,
) {
    fun start() {
        this.isProcess = true
    }

    fun end() {
        this.isProcess = false
    }
}
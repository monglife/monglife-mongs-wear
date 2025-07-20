package com.monglife.mongs.presentation.viewmodel.pages.training.runner.vo

data class TrainingRunnerVo(
    val isProcess: Boolean,
    val score: Int,
    val hurdleVos: List<HurdleVo>,
    val trainingPlayerVo: TrainingPlayerVo,
)

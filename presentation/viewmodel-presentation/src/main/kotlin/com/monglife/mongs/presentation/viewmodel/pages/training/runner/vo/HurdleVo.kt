package com.monglife.mongs.presentation.viewmodel.pages.training.runner.vo

data class HurdleVo(
    val height: Int,
    val width: Int,
    var py: Float,
    var px: Float,
    var isContainedScore: Boolean = false,
)

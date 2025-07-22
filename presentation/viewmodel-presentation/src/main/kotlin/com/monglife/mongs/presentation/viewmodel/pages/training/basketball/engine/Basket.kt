package com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine

data class Basket(
    val height: Float,
    val width: Float,
    private val topInitY: Float,
    private val topInitX: Float,
    val radius: Float = height / 2,
    var py: Float = topInitY,
    var px: Float = topInitX,
    var ly: Float = topInitY,
    var lx: Float = topInitX - width / 2,
    var ry: Float = topInitY,
    var rx: Float = topInitX + width / 2,
)
package com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine

data class Basket(
    val height: Float,
    val width: Float,
    private val initY: Float,
    private val initX: Float,
    val radius: Float = height / 2,
    var py: Float = initY,
    var px: Float = initX,
    var ly: Float = initY,
    var lx: Float = initX - width / 2,
    var ry: Float = initY,
    var rx: Float = initX + width / 2,
)
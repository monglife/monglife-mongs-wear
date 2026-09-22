package com.monglife.mongs.presentation.viewmodel.pages.training.basketball.vo

import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine.Basket

data class BasketVo(
    val height: Float,
    val width: Float,
    val radius: Float,
    val py: Float,
    val px: Float,
) {
    companion object {
        fun of(basket: Basket) = BasketVo(
            height = basket.height,
            width = basket.width,
            radius = basket.radius,
            py = basket.py,
            px = basket.px,
        )
    }
}

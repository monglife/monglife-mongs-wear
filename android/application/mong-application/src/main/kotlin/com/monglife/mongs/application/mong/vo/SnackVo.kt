package com.monglife.mongs.application.mong.vo

import com.monglife.mongs.domain.mong.model.Snack

data class SnackVo(
    val snackCode: String,
    val snackName: String,
    val price: Int,
    val isCanBuy: Boolean,
    val weight: Double,
    val strength: Double,
    val satiety: Double,
    val healthy: Double,
    val fatigue: Double,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(snack: Snack): SnackVo {
            return SnackVo(
                snackCode = snack.snackCode,
                snackName = snack.snackName,
                price = snack.price,
                isCanBuy = snack.isCanBuy,
                weight = snack.weight,
                strength = snack.strength,
                satiety = snack.satiety,
                healthy = snack.healthy,
                fatigue = snack.fatigue,
            )
        }
    }
}
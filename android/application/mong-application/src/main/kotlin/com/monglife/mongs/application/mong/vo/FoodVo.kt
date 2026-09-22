package com.monglife.mongs.application.mong.vo

import com.monglife.mongs.domain.mong.model.Food

data class FoodVo(
    val foodCode: String,
    val foodName: String,
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
        fun of(food: Food): FoodVo {
            return FoodVo(
                foodCode = food.foodCode,
                foodName = food.foodName,
                price = food.price,
                isCanBuy = food.isCanBuy,
                weight = food.weight,
                strength = food.strength,
                satiety = food.satiety,
                healthy = food.healthy,
                fatigue = food.fatigue,
            )
        }
    }
}








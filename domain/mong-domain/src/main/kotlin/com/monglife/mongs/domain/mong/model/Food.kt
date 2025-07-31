package com.monglife.mongs.domain.mong.model

class Food(
    foodCode: String,
    foodName: String,
    price: Int,
    isCanBuy: Boolean,
    weight: Double,
    strength: Double,
    satiety: Double,
    healthy: Double,
    fatigue: Double,
) {
    var foodCode: String = foodCode
        private set
    var foodName: String = foodName
        private set
    var price: Int = price
        private set
    var isCanBuy: Boolean = isCanBuy
        private set
    var weight: Double = weight
        private set
    var strength: Double = strength
        private set
    var satiety: Double = satiety
        private set
    var healthy: Double = healthy
        private set
    var fatigue: Double = fatigue
        private set
}










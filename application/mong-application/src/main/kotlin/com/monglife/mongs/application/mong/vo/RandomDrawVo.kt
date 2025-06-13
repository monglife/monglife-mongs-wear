package com.monglife.mongs.application.mong.vo

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.model.RandomDraw

data class RandomDrawVo(

    val randomDrawCode: String,

    val randomDrawName: String,

    val inventoryTypeCode: InventoryTypeCode,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(randomDraw: RandomDraw): RandomDrawVo {
            return RandomDrawVo(
                randomDrawCode = randomDraw.randomDrawCode,
                randomDrawName = randomDraw.randomDrawName,
                inventoryTypeCode = randomDraw.inventoryTypeCode,
            )
        }
    }
}
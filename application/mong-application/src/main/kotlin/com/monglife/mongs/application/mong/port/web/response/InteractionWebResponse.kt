package com.monglife.mongs.application.mong.port.web.response

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.domain.mong.model.Food
import com.monglife.mongs.domain.mong.model.Inventory
import com.monglife.mongs.domain.mong.model.RandomDraw
import com.monglife.mongs.domain.mong.model.Snack

/**
 * 몽 먹이 조회 응답
 */
data class GetFoodResponse(
    val foodCode: String,
    val foodName: String,
    val price: Int,
    val weight: Double,
    val strength: Double,
    val satiety: Double,
    val healthy: Double,
    val fatigue: Double,
    val isCanBuy: Boolean,
) {
    fun toDomain(): Food {
        return Food(
            foodCode = foodCode,
            foodName = foodName,
            price = price,
            weight = weight,
            strength = strength,
            satiety = satiety,
            healthy = healthy,
            fatigue = fatigue,
            isCanBuy = isCanBuy,
        )
    }
}

/**
 * 몽 간식 조회 응답
 */
data class GetSnackResponse(
    val snackCode: String,
    val snackName: String,
    val price: Int,
    val weight: Double,
    val strength: Double,
    val satiety: Double,
    val healthy: Double,
    val fatigue: Double,
    val isCanBuy: Boolean,
) {
    fun toDomain(): Snack {
        return Snack(
            snackCode = snackCode,
            snackName = snackName,
            price = price,
            weight = weight,
            strength = strength,
            satiety = satiety,
            healthy = healthy,
            fatigue = fatigue,
            isCanBuy = isCanBuy,
        )
    }
}

/**
 * 몽 먹이 주기 응답
 */
data class FeedFoodMongResponse(
    val mongId: Long,
    val payPoint: Int,
    val expRatio: Double,
    val strengthRatio: Double,
    val healthyRatio: Double,
    val satietyRatio: Double,
    val fatigueRatio: Double,
    val weight: Double,
    val stateCode: MongStateCode,
    val statusCode: MongStatusCode,
)

/**
 * 몽 간식 주기 응답
 */
data class FeedSnackMongResponse(
    val mongId: Long,
    val payPoint: Int,
    val expRatio: Double,
    val strengthRatio: Double,
    val healthyRatio: Double,
    val satietyRatio: Double,
    val fatigueRatio: Double,
    val weight: Double,
    val stateCode: MongStateCode,
    val statusCode: MongStatusCode,
)

/**
 * 인벤토리 조회 응답
 */
data class GetInventoryResponse(
    val inventoryId: Long,
    val mongId: Long,
    val inventoryCode: String,
    val inventoryName: String,
    val inventoryTypeCode: InventoryTypeCode,
) {
    fun toDomain(): Inventory {
        return Inventory(
            mongId = mongId,
            inventoryId = inventoryId,
            inventoryCode = inventoryCode,
            inventoryName = inventoryName,
            inventoryTypeCode = inventoryTypeCode,
        )
    }
}

/**
 * 인벤토리 소비 응답
 */
data class ConsumeInventoryResponse(
    val mongId: Long,
    val payPoint: Int,
    val expRatio: Double,
    val strengthRatio: Double,
    val healthyRatio: Double,
    val satietyRatio: Double,
    val fatigueRatio: Double,
    val weight: Double,
    val stateCode: MongStateCode,
    val statusCode: MongStatusCode,
)

/**
 * 랜덤 뽑기 티켓 구매 응답
 */
data class BuyRandomDrawTicketResponse(
    val mongId: Long,
    val payPoint: Int,
    val randomDrawTicketCount: Int,
)

/**
 * 랜덤 뽑기 응답
 */
data class RandomDrawResponseVo(
    val randomDrawCode: String,
    val randomDrawName: String,
    val inventoryTypeCode: InventoryTypeCode,
) {
    fun toDomain(): RandomDraw {
        return RandomDraw(
            randomDrawCode = randomDrawCode,
            randomDrawName = randomDrawName,
            inventoryTypeCode = inventoryTypeCode,
        )
    }
}
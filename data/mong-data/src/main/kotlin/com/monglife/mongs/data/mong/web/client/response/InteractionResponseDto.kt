package com.monglife.mongs.data.mong.web.client.response

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode

/**
 * 몽 먹이 조회 응답 Dto
 */
data class GetFoodResponseDto(
    val foodCode: String,
    val foodName: String,
    val price: Int,
    val weight: Double,
    val strength: Double,
    val satiety: Double,
    val healthy: Double,
    val fatigue: Double,
    val isCanBuy: Boolean,
)

/**
 * 몽 간식 조회 응답 Dto
 */
data class GetSnackResponseDto(
    val snackCode: String,
    val snackName: String,
    val price: Int,
    val weight: Double,
    val strength: Double,
    val satiety: Double,
    val healthy: Double,
    val fatigue: Double,
    val isCanBuy: Boolean,
)

/**
 * 음식 섭취 응답 Dto
 */
data class FeedFoodResponseDto(
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
 * 간식 섭취 응답 Dto
 */
data class FeedSnackResponseDto(
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
 * 인벤토리 조회 응답 Dto
 */
data class GetInventoryResponseDto(
    val inventoryId: Long,
    val mongId: Long,
    val inventoryCode: String,
    val inventoryName: String,
    val inventoryTypeCode: InventoryTypeCode,
)

/**
 * 인벤토리 소비 응답 Dto
 */
data class UseInventoryResponseDto(
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
 * 랜덤 뽑기 티켓 구매 응답 Dto
 */
data class BuyRandomDrawTicketResponseDto(
    val mongId: Long,
    val payPoint: Int,
    val randomDrawTicketCount: Int,
)

/**
 * 랜덤 뽑기 응답 Dto
 */
data class RandomDrawResponseDto(
    val randomDrawCode: String,
    val randomDrawName: String,
    val inventoryTypeCode: InventoryTypeCode,
)
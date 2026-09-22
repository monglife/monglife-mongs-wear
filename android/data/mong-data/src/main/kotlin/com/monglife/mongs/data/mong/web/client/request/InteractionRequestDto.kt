package com.monglife.mongs.data.mong.web.client.request

/**
 * 음식 섭취 요청 Dto
 */
data class FeedFoodRequestDto(
    val foodCode: String,
)

/**
 * 간식 섭취 요청 Dto
 */
data class FeedSnackRequestDto(
    val snackCode: String,
)

/**
 * 인벤토리 소비 요청 Dto
 */
data class UseInventoryRequestDto(
    val inventoryId: Long,
)
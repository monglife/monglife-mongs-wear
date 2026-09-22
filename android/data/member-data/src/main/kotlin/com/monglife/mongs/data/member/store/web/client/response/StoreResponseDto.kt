package com.monglife.mongs.data.member.store.web.client.response

/**
 * 인앱 상품 목록 조회 응답 Dto
 */
data class GetProductResponseDto(
    val productId: String,
    val productName: String,
    val price: Double,
)

/**
 * 소비된 주문 목록 조회 응답 Dto
 */
data class GetConsumedOrderResponseDto(
    val orderId: Long,
    val socialOrderId: String,
    val productId: String,
)

/**
 * 주문 소비 응답 Dto
 */
data class ConsumeOrderResponseDto(
    val accountId: Long,
    val orderId: Long,
    val socialOrderId: String,
    val productId: String,
    val purchaseToken: String,
    val starPoint: Int,
    val slotCount: Int,
)

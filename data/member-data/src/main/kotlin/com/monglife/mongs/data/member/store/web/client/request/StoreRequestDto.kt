package com.monglife.mongs.data.member.store.web.client.request

/**
 * 소비된 주문 목록 조회 요청 Dto
 */
data class GetConsumedOrdersRequestDto(
    val socialOrderIds: List<String>,
)

/**
 * 주문 소비 요청 Dto
 */
data class ConsumeOrderRequestDto(
    val socialOrderId: String,
    val productId: String,
    val purchaseToken: String,
)
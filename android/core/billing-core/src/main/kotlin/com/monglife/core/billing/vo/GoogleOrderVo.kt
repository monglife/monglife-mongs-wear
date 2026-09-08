package com.monglife.core.billing.vo

/**
 * 구글 주문 완료 정보 Vo
 */
data class GoogleOrderVo(
    val productId: String,
    val socialOrderId: String,
    val purchaseToken: String,
)
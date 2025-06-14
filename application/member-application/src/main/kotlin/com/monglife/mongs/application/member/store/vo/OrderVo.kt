package com.monglife.mongs.application.member.store.vo

import com.monglife.mongs.domain.member.store.model.Order

data class OrderVo(
    val orderId: Long,
    val socialOrderId: String,
    val productId: String,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(order: Order) = OrderVo(
            orderId = order.orderId,
            socialOrderId = order.socialOrderId,
            productId = order.productId,
        )
    }
}
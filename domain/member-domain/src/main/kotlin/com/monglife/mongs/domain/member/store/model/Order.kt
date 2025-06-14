package com.monglife.mongs.domain.member.store.model

class Order(
    orderId: Long,
    socialOrderId: String,
    productId: String,
) {
    var orderId: Long = orderId
        private set
    var socialOrderId: String = socialOrderId
        private set
    var productId: String = productId
        private set
}
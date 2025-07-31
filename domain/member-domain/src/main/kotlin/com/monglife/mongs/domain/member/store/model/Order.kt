package com.monglife.mongs.domain.member.store.model

class Order(
    socialOrderId: String,
    productId: String,
    purchaseToken: String,
) {
    var socialOrderId: String = socialOrderId
        private set
    var productId: String = productId
        private set
    var purchaseToken: String = purchaseToken
        private set
}
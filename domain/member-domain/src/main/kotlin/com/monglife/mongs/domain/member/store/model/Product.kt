package com.monglife.mongs.domain.member.store.model

class Product(
    productId: String,
    productName: String,
    price: Double,
) {
    var productId: String = productId
        private set
    var productName: String = productName
        private set
    var price: Double = price
        private set
}

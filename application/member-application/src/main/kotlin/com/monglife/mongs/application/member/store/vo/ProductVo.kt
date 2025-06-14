package com.monglife.mongs.application.member.store.vo

import com.monglife.mongs.domain.member.store.model.Product

data class ProductVo(
    val productId: String,
    val productName: String,
    val price: Double,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(product: Product) = ProductVo(
            productId = product.productId,
            productName = product.productName,
            price = product.price,
        )
    }
}

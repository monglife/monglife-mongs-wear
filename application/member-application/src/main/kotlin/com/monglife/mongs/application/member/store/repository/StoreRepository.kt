package com.monglife.mongs.domain.member.store.repository

interface StoreRepository {

    /**
     * 상품 ID 목록 조회
     */
    suspend fun getProductIds(): List<String>

    /**
     * 소비 상품 ID 목록 조회
     */
    suspend fun getConsumedOrderIds(orderIds: List<String>): List<String>

    /**
     * 상품 주문 소비
     */
    suspend fun consumeProductOrder(productId: String, orderId: String, purchaseToken: String)
}
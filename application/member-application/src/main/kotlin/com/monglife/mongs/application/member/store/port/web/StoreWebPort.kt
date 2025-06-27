package com.monglife.mongs.application.member.store.port.web

import com.monglife.mongs.application.member.store.exception.InvalidConsumeOrderException
import com.monglife.mongs.application.member.store.port.web.response.ConsumeOrderResponse
import com.monglife.mongs.application.member.store.port.web.response.GetNotConsumedOrderResponse
import com.monglife.mongs.application.member.store.port.web.response.GetProductResponse

interface StoreWebPort {

    /**
     * 인앱 상품 목록 조회
     */
    suspend fun getProducts(): List<GetProductResponse>

    /**
     * 소비된 주문 목록 조회
     */
    suspend fun getNotConsumedOrders(): List<GetNotConsumedOrderResponse>

    /**
     * 주문 소비
     */
    @Throws(InvalidConsumeOrderException::class)
    suspend fun consumeOrder(socialOrderId: String, productId: String, purchaseToken: String): ConsumeOrderResponse
}
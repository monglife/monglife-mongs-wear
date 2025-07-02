package com.monglife.mongs.data.member.store.web.adapter

import com.monglife.core.billing.client.GoogleBillingClient
import com.monglife.mongs.application.member.store.exception.InvalidConsumeOrderException
import com.monglife.mongs.application.member.store.port.web.StoreWebPort
import com.monglife.mongs.application.member.store.port.web.response.ConsumeOrderResponse
import com.monglife.mongs.application.member.store.port.web.response.GetNotConsumedOrderResponse
import com.monglife.mongs.application.member.store.port.web.response.GetProductResponse
import com.monglife.mongs.data.member.store.web.client.StoreWebClient
import com.monglife.mongs.data.member.store.web.client.request.ConsumeOrderRequestDto
import com.monglife.mongs.data.member.store.web.client.request.GetConsumedOrdersRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreWebAdapter @Inject constructor(
    private val storeWebClient: StoreWebClient,
    private val googleBillingClient: GoogleBillingClient,
) : StoreWebPort {

    /**
     * 인앱 상품 목록 조회
     */
    override suspend fun getProducts(): List<GetProductResponse> =
        storeWebClient.getProducts().let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            body?.result?.map {
                GetProductResponse(
                    productId = it.productId,
                    productName = it.productName,
                    price = it.price,
                )
            } ?: emptyList()
        }

    /**
     * 소비 전 주문 목록 조회
     */
    override suspend fun getNotConsumedOrders(): List<GetNotConsumedOrderResponse> {

        val notConsumedGoogleOrderVos = googleBillingClient.getNotConsumedGoogleOrders()

        if (notConsumedGoogleOrderVos.isEmpty()) {
            return emptyList()
        }

        return storeWebClient.getConsumedOrders(
            getConsumedOrdersRequestDto = GetConsumedOrdersRequestDto(
                socialOrderIds = notConsumedGoogleOrderVos.map { it.socialOrderId },
            )
        ).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            val getConsumedOrderResponseDtos = body?.result ?: emptyList()

            notConsumedGoogleOrderVos.filter { notConsumedGoogleOrderVo ->
                    !getConsumedOrderResponseDtos
                        .map { it.socialOrderId }
                        .contains(notConsumedGoogleOrderVo.socialOrderId)
                }.map {
                    GetNotConsumedOrderResponse(
                        socialOrderId = it.socialOrderId,
                        productId = it.productId.uppercase(),
                        purchaseToken = it.purchaseToken,
                    )
                }
        }
    }

    /**
     * 주문 소비
     */
    @Throws(InvalidConsumeOrderException::class)
    override suspend fun consumeOrder(
        socialOrderId: String,
        productId: String,
        purchaseToken: String
    ): ConsumeOrderResponse = storeWebClient.consumeOrder(
        consumeOrderRequestDto = ConsumeOrderRequestDto(
            socialOrderId = socialOrderId,
            productId = productId,
            purchaseToken = purchaseToken,
        )
    ).let { response ->

        val body =
            response.takeIf { it.isSuccessful }?.body() ?: throw InvalidConsumeOrderException()

        ConsumeOrderResponse(
            accountId = body.result.accountId,
            orderId = body.result.orderId,
            socialOrderId = body.result.socialOrderId,
            productId = body.result.productId,
            purchaseToken = body.result.purchaseToken,
            starPoint = body.result.starPoint,
            slotCount = body.result.slotCount,
        )
    }
}
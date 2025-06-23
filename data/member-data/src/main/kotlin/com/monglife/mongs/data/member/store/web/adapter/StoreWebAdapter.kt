package com.monglife.mongs.data.member.store.web.adapter

import android.content.Context
import android.os.Build
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.QueryPurchasesParams
import com.monglife.mongs.application.member.store.exception.BillingNotSupportException
import com.monglife.mongs.application.member.store.exception.InvalidConsumeOrderException
import com.monglife.mongs.application.member.store.exception.InvalidGetConsumedOrdersException
import com.monglife.mongs.application.member.store.port.web.StoreWebPort
import com.monglife.mongs.application.member.store.port.web.response.ConsumeOrderResponse
import com.monglife.mongs.application.member.store.port.web.response.GetConsumedOrderResponse
import com.monglife.mongs.application.member.store.port.web.response.GetProductResponse
import com.monglife.mongs.data.member.store.web.client.StoreWebClient
import com.monglife.mongs.data.member.store.web.client.request.ConsumeOrderRequestDto
import com.monglife.mongs.data.member.store.web.client.request.GetConsumedOrdersRequestDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class StoreWebAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storeWebClient: StoreWebClient,
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
     * 소비된 주문 목록 조회
     */
    override suspend fun getConsumedOrders(): List<GetConsumedOrderResponse> {

        val purchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.INAPP)
            .build()

        val billingClient = getBillingClient()

        val socialOrderIds = suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(purchasesParams, { billingResult, purchases ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    cont.resume(purchases.map { purchase -> purchase.orderId ?: "-" })
                } else {
                    cont.resumeWithException(InvalidGetConsumedOrdersException())
                }
            })
        }

        return storeWebClient.getConsumedOrders(
            getConsumedOrdersRequestDto = GetConsumedOrdersRequestDto(
                socialOrderIds = socialOrderIds
            )
        ).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            body?.result?.map {
                GetConsumedOrderResponse(
                    orderId = it.orderId,
                    socialOrderId = it.socialOrderId,
                    productId = it.productId,
                )
            } ?: emptyList()
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

    /**
     * Billing Client 생성
     */
    private suspend fun getBillingClient(): BillingClient = suspendCancellableCoroutine { cont ->

        val billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        if (billingClient.connectionState == ConnectionState.DISCONNECTED) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        cont.resume(billingClient)
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        cont.resumeWithException(BillingNotSupportException())
                    } else {
                        cont.resumeWithException(InvalidConsumeOrderException())
                    }
                }

                override fun onBillingServiceDisconnected() {}
            })
        } else {
            cont.resume(billingClient)
        }
    }
}
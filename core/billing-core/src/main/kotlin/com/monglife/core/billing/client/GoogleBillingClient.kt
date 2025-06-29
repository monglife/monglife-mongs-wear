package com.monglife.core.billing.client

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.monglife.core.billing.exception.AlreadyOwnedException
import com.monglife.core.billing.exception.BillingConnectException
import com.monglife.core.billing.exception.BillingNotSupportException
import com.monglife.core.billing.exception.InvalidBillingException
import com.monglife.core.billing.exception.InvalidGetConsumedOrdersException
import com.monglife.core.billing.exception.UserCancelException
import com.monglife.core.billing.vo.GoogleOrderVo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class GoogleBillingClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * 구글 주문 흐름 시작
     */
    fun billing(activity: Activity, productId: String): Flow<GoogleOrderVo> = callbackFlow {
        val billingClient =
            getBillingClient(listener = { billingResult: BillingResult, purchases: MutableList<Purchase>? ->
                when (billingResult.responseCode) {
                    BillingResponseCode.OK -> {
                        purchases?.let {
                            for (purchase in purchases) {
                                trySend(
                                    GoogleOrderVo(
                                        productId = purchase.products[0],
                                        socialOrderId = purchase.orderId ?: "-",
                                        purchaseToken = purchase.purchaseToken,
                                    )
                                )
                            }
                        } ?: run { close(InvalidBillingException()) }
                    }
                    BillingResponseCode.USER_CANCELED -> close(UserCancelException())
                    BillingResponseCode.BILLING_UNAVAILABLE -> close(UserCancelException())
                    BillingResponseCode.ITEM_ALREADY_OWNED -> close(AlreadyOwnedException())
                    BillingResponseCode.ERROR -> close(InvalidBillingException())
                    else -> close(BillingNotSupportException())
                }
            })

        val productDetailsParam = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    Product.newBuilder()
                        .setProductId(productId.lowercase())
                        .setProductType(ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetails(productDetailsParam).let {
            if (it.billingResult.responseCode == BillingResponseCode.OK) {
                it.productDetailsList?.let { productDetailsList ->
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetailsList[0])
                                    .build()
                            )
                        )
                        .build()

                    billingClient.launchBillingFlow(activity, billingFlowParams)

                } ?: run {
                    close(InvalidBillingException())
                }
            } else {
                close(InvalidBillingException())
            }
        }

        awaitClose {
            billingClient.endConnection()
        }
    }

    /**
     * 소비 전 구글 주문 목록 조회
     */
    suspend fun getNotConsumedGoogleOrders(): List<GoogleOrderVo> {

        val billingClient = getBillingClient(listener = { _, _ -> })

        return suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(ProductType.INAPP)
                    .build(),
                { billingResult, purchases ->
                    when (billingResult.responseCode) {
                        BillingResponseCode.OK -> {
                            cont.resume(
                                purchases.map { purchase ->
                                    GoogleOrderVo(
                                        productId = purchase.products[0],
                                        socialOrderId = purchase.orderId ?: "-",
                                        purchaseToken = purchase.purchaseToken,
                                    )
                                })
                        }
                        else -> {
                            cont.resumeWithException(InvalidGetConsumedOrdersException())
                        }
                    }

                    billingClient.endConnection()
                }
            )
        }
    }

    /**
     * Billing Client 생성
     */
    private suspend fun getBillingClient(listener: PurchasesUpdatedListener): BillingClient = suspendCancellableCoroutine { cont ->

        val billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener(listener)
            .build()

        if (billingClient.connectionState == ConnectionState.DISCONNECTED) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        cont.resume(billingClient)
                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        cont.resumeWithException(BillingNotSupportException())
                    } else {
                        cont.resumeWithException(BillingConnectException())
                    }
                }

                override fun onBillingServiceDisconnected() {}
            })
        } else {
            cont.resume(billingClient)
        }
    }
}
package com.monglife.core.billing.client

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import com.monglife.core.billing.exception.AlreadyOwnedException
import com.monglife.core.billing.exception.BillingConnectException
import com.monglife.core.billing.exception.BillingNotSupportException
import com.monglife.core.billing.exception.InvalidBillingException
import com.monglife.core.billing.exception.InvalidGetConsumedOrdersException
import com.monglife.core.billing.exception.PendingPurchaseException
import com.monglife.core.billing.exception.UserCancelException
import com.monglife.core.billing.vo.GoogleOrderVo
import com.monglife.core.common.exception.ErrorException
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
    companion object {
        /**
         * 결제 경로 추적 태그
         *
         * 돈이 걸린 경로인데 실패해도 아무 흔적이 없었다. 워치 결제는 폰으로 인계되면서
         * 앱이 백그라운드로 내려가 재현이 어려우므로, 콜백이 언제 무엇을 들고 오는지를
         * 남겨 두지 않으면 사후에 재구성할 방법이 없다.
         *
         * Log.i 는 릴리스에서 R8 이 지운다(proguard/mongs-release.pro). 프로덕션 비용은 0 이다.
         * 흐름을 끝내는 실패만 Log.w 로 남겨 릴리스에서도 보이게 한다.
         */
        private const val TAG = "Billing"
    }

    /**
     * 구글 주문 흐름 시작
     */
    fun billing(activity: Activity, productId: String): Flow<GoogleOrderVo> = callbackFlow {
        val billingClient =
            getBillingClient(listener = { billingResult: BillingResult, purchases: MutableList<Purchase>? ->
                // 폰 인계 시점에 이 콜백이 오는지, 온다면 무엇을 들고 오는지가 진단의 핵심이다.
                Log.i(TAG, "onPurchasesUpdated code=${billingResult.responseCode} purchases=${purchases?.size ?: -1} ${purchases.orEmpty().joinToString { "[state=${it.purchaseState} orderId=${it.orderId?.let { _ -> "있음" } ?: "없음"} products=${it.products}]" }}")

                when (billingResult.responseCode) {
                    BillingResponseCode.OK -> {
                        /**
                         * 어떤 응답이 와도 emit 하거나 close 하거나 둘 중 하나는 반드시 한다.
                         *
                         * 이전에는 purchases 가 non-null 이면서 빈 리스트일 때 for 문이 돌지 않고
                         * 엘비스 분기도 타지 않아 emit 도 close 도 없이 flow 가 열린 채 남았다.
                         * 그러면 collector 의 first() 가 영구 대기해 로딩바가 고착된다.
                         */
                        val purchaseList = purchases.orEmpty()
                        val googleOrderVos = purchaseList.mapNotNull { it.toGoogleOrderVoOrNull() }

                        if (googleOrderVos.isEmpty()) {
                            // 구매는 있는데 소비 가능한 것이 없다 = 전부 승인 대기중
                            val reason =
                                if (purchaseList.isEmpty()) InvalidBillingException()
                                else PendingPurchaseException()

                            Log.w(TAG, "흐름 종료 - 소비 가능한 구매 없음 (${reason::class.simpleName})")
                            close(reason)
                        } else {
                            Log.i(TAG, "구매 emit ${googleOrderVos.size}건")
                            googleOrderVos.forEach { trySend(it) }
                        }
                    }
                    else -> {
                        Log.w(TAG, "흐름 종료 - 응답 코드 ${billingResult.responseCode} ${billingResult.debugMessage}")
                        close(billingResult.toBillingException())
                    }
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

        queryProductDetails(
            billingClient = billingClient,
            params = productDetailsParam,
        ).productDetailsList.firstOrNull()?.let { productDetails ->
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()

            /**
             * 반환값을 버리면 결제 UI 가 아예 뜨지 않은 경우(DEVELOPER_ERROR, Activity 종료 중 등)
             * 리스너가 영영 호출되지 않아 flow 가 매달린다.
             */
            val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)

            Log.i(TAG, "launchBillingFlow code=${launchResult.responseCode} ${launchResult.debugMessage}")

            if (launchResult.responseCode != BillingResponseCode.OK) {
                close(launchResult.toBillingException())
            }

        } ?: run {
            Log.w(TAG, "흐름 종료 - 상품 정보 없음 productId=$productId")
            close(InvalidBillingException())
        }

        awaitClose {
            // 여기가 불리면 결제 세션이 끊긴다. Play 결제 화면이 아직 떠 있는 동안
            // 불리고 있지 않은지가 "화면이 안 꺼진다" 의 판정 지점이다.
            Log.i(TAG, "awaitClose - endConnection")
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
                            cont.resume(purchases.mapNotNull { it.toGoogleOrderVoOrNull() })
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
     * 상품 정보 조회
     */
    private suspend fun queryProductDetails(
        billingClient: BillingClient,
        params: QueryProductDetailsParams,
    ): QueryProductDetailsResult = suspendCancellableCoroutine { cont ->

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                cont.resume(queryProductDetailsResult)
            } else {
                cont.resumeWithException(InvalidBillingException())
            }
        }
    }

    /**
     * Purchase → GoogleOrderVo 변환
     *
     * - PURCHASED 가 아닌 구매(PENDING 등)는 아직 소비할 수 없으므로 null 로 걸러낸다.
     * - Play 의 상품 ID 는 소문자, 서버/도메인 규약은 대문자다. Play 로 나갈 때만
     *   lowercase 를 쓰고, 밖으로 내보내는 값은 여기서 uppercase 로 고정한다.
     *   이 정규화가 어긋나면 GetProductsUseCase 의 productId 조인이 실패해
     *   미소비 주문의 "소비" 버튼이 아예 뜨지 않는다.
     */
    private fun Purchase.toGoogleOrderVoOrNull(): GoogleOrderVo? =
        takeIf { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            ?.let {
                GoogleOrderVo(
                    productId = it.products[0].uppercase(),
                    socialOrderId = it.orderId ?: "-",
                    purchaseToken = it.purchaseToken,
                )
            }

    /**
     * 응답 코드 → 예외 매핑
     *
     * 리스너와 launchBillingFlow 검사 두 곳에서 쓴다.
     */
    private fun BillingResult.toBillingException(): ErrorException = when (responseCode) {
        BillingResponseCode.USER_CANCELED -> UserCancelException()
        BillingResponseCode.BILLING_UNAVAILABLE -> BillingNotSupportException()
        BillingResponseCode.ITEM_ALREADY_OWNED -> AlreadyOwnedException()
        BillingResponseCode.ERROR -> InvalidBillingException()
        else -> BillingNotSupportException()
    }

    /**
     * Billing Client 생성
     */
    private suspend fun getBillingClient(listener: PurchasesUpdatedListener): BillingClient = suspendCancellableCoroutine { cont ->

        val billingClient = BillingClient.newBuilder(context)
            .enableAutoServiceReconnection()
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener(listener)
            .build()

        /**
         * 갓 build 한 인스턴스는 항상 DISCONNECTED 라 connectionState 분기는 의미가 없었다.
         * 그보다 중요한 것은 이 콜백이 "한 번만" 오지 않는다는 점이다.
         *
         * enableAutoServiceReconnection 을 켜면 라이브러리가 ServiceConnection 에
         * BillingClientStateListener 를 보관해 두고(zzbz.zzb) 재연결마다 같은 인스턴스의
         * onBillingSetupFinished 를 다시 호출한다. 그때 이미 resume 된 continuation 을
         * 또 resume 하면 IllegalStateException 이 나는데, 라이브러리가 이를 삼키면서
         * "Exception while calling onBillingSetupFinished." 로그만 남기고 결제 세션이 깨진다.
         *
         * 폰으로 결제가 넘어가면 앱이 백그라운드로 내려가 바인딩이 끊기기 쉬워 실제로 밟힌다.
         */
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                // 자동 재연결마다 다시 불린다. 두 번째부터는 무시되는 것이 정상이다.
                Log.i(TAG, "onBillingSetupFinished code=${billingResult.responseCode} active=${cont.isActive}")

                if (!cont.isActive) return

                if (billingResult.responseCode == BillingResponseCode.OK) {
                    cont.resume(billingClient)
                } else {
                    cont.resumeWithException(BillingConnectException())
                }
            }

            override fun onBillingServiceDisconnected() {
                // 폰 인계로 앱이 백그라운드에 내려가면 흔히 끊긴다.
                // enableAutoServiceReconnection 이 다시 붙여 주는지 뒤따르는 로그로 확인한다.
                Log.i(TAG, "onBillingServiceDisconnected")
            }
        })

        // 연결을 기다리는 동안 취소되면 클라이언트가 그대로 남으므로 여기서 정리한다.
        cont.invokeOnCancellation { billingClient.endConnection() }
    }
}

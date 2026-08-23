package com.monglife.mongs.presentation.viewmodel.pages.charge

import android.app.Activity
import com.monglife.core.billing.client.GoogleBillingClient
import com.monglife.core.billing.exception.BillingNotSupportException
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.usecase.ObservePlayerUseCase
import com.monglife.mongs.application.member.store.usecase.ConsumeProductOrderUseCase
import com.monglife.mongs.application.member.store.usecase.GetProductsUseCase
import com.monglife.mongs.application.member.store.vo.OrderVo
import com.monglife.mongs.application.member.store.vo.ProductVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChargeStarPointViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val observePlayerUseCase: ObservePlayerUseCase,
    private val consumeProductOrderUseCase: ConsumeProductOrderUseCase,
    private val billingClient: GoogleBillingClient,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val content: Boolean = true,
    ) {
        data object Idle : UiState()

        /** 최초 진입 · 재조회 — 아직 표시할 데이터가 없다 */
        data object Loading : UiState(loadingBar = true, content = false)

        /**
         * 결제 진행 중 — 데이터는 이미 있으므로 화면을 유지한 채 오버레이만 띄운다.
         *
         * Loading 으로 화면을 통째로 가리면 미소비 주문의 "소비" 버튼도 함께 사라져,
         * 결제 콜백이 오지 않을 때 사용자가 복구할 방법이 없어진다.
         */
        data object Billing : UiState(loadingBar = true, content = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class Buy(val message: String): UiEvent()
        data class Consume(val message: String): UiEvent()
        data class NavMain(val message: String): UiEvent()
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * UI 이벤트 변수
     */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    /**
     * 변수
     */
    private val _starPoint = MutableStateFlow(0)
    val starPoint: StateFlow<Int> = _starPoint.asStateFlow()

    private val _productVos = MutableStateFlow<List<ProductVo>>(emptyList())
    val productVos: StateFlow<List<ProductVo>> = _productVos.asStateFlow()

    private var resumeJob: Job? = null

    /** 최초 ON_RESUME 은 init 의 조회와 겹치므로 한 번 건너뛴다. */
    private var isFirstResume = true

    /**
     * 소비를 시도한 purchaseToken.
     *
     * 실시간 리스너 경로(orderAndConsume)와 회수 경로(autoConsume)가 같은 주문을
     * 각각 잡을 수 있으므로 가드를 공유한다. 기록은 요청 "전에" 남겨, 실패로
     * 빠져나가도 자동 재시도가 돌지 않게 한다.
     */
    private val consumeAttemptedTokens = mutableSetOf<String>()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            try {
                withContext(Dispatchers.IO) {
                    getProductsUseCase().let {
                        if (it.isNotEmpty()) {
                            _productVos.value = it
                        } else {
                            delay(NAVIGATE_DELAY)
                            _uiEvent.emit(UiEvent.NavMain("인앱 상품 없음"))
                        }
                    }

                    observeForever(observePlayerUseCase().map {  it.starPoint }, _starPoint)

                    autoConsume()
                }
            } finally {
                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 인앱 상품 주문 및 소비
     */
    fun orderAndConsume(productId: String, activity: Activity) {
        // 결제 중에도 화면이 보이므로 버튼 재탭으로 결제가 중복 실행되지 않게 막는다
        if (_uiState.value is UiState.Billing) return

        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Billing

            try {
                val googleOrderVo = billingClient.billing(activity = activity, productId = productId)
                    .first()

                withContext(Dispatchers.IO) {
                    // 회수 경로가 이미 소비했다면 여기서는 건너뛴다
                    val consumed = consumeOnce(
                        productId = googleOrderVo.productId,
                        socialOrderId = googleOrderVo.socialOrderId,
                        purchaseToken = googleOrderVo.purchaseToken,
                    )

                    _productVos.value = getProductsUseCase()

                    if (consumed) {
                        _uiEvent.emit(UiEvent.Buy("충전 완료"))
                    }
                }
            } finally {
                /**
                 * 예외뿐 아니라 취소로도 여기를 지나야 한다.
                 * suspend 호출(_uiEvent.emit, delay)은 취소 상태에서 실행되지 않으므로 두지 않는다.
                 */
                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 인앱 상품 소비
     */
    fun consume(orderVo: OrderVo) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            try {
                withContext(Dispatchers.IO) {
                    // 사용자가 직접 누른 재시도이므로 자동 소비 가드를 우회한다
                    consumeOnce(
                        productId = orderVo.productId,
                        socialOrderId = orderVo.socialOrderId,
                        purchaseToken = orderVo.purchaseToken,
                        force = true,
                    )

                    _productVos.value = getProductsUseCase()
                    _uiEvent.emit(UiEvent.Consume("소비 완료"))
                }
            } finally {
                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 화면 복귀 처리
     *
     * 폰에서 완료된 결제가 워치의 PurchasesUpdatedListener 로 오지 않는 경우가 실재한다.
     * Google 권장 모델대로 queryPurchasesAsync(= getProductsUseCase 경로)를 정합성의
     * 근원으로 삼아 미소비 주문을 회수한다.
     *
     * 진행 중인 결제에는 간섭하지 않는다. 대기 중인 코루틴을 취소하면 실시간 경로가
     * 죽어버리므로, 재조회만 얹고 결제는 그대로 둔다.
     */
    fun onResume() {
        /**
         * getProductsUseCase 는 BillingClient 연결과 서버 POST 를 동반하므로
         * init 의 조회와 겹치는 최초 1회는 건너뛴다.
         */
        if (isFirstResume) {
            isFirstResume = false
            return
        }

        if (resumeJob?.isActive == true) return

        resumeJob = viewModelScopeWithHandler.launch(Dispatchers.Main) {
            // 결제 진행 중이면 Billing 상태를 유지한다
            val isBilling = _uiState.value is UiState.Billing

            if (!isBilling) {
                _uiState.value = UiState.Loading
            }

            try {
                withContext(Dispatchers.IO) {
                    _productVos.value = getProductsUseCase()

                    autoConsume()
                }
            } finally {
                if (!isBilling) {
                    _uiState.value = UiState.Idle
                }
            }
        }
    }

    /**
     * 가드를 통과한 주문만 실제로 소비한다.
     *
     * @param force 사용자가 직접 누른 재시도. 세션 내 1회 제한을 우회한다.
     * @return 실제로 소비 요청을 보냈으면 true
     */
    private suspend fun consumeOnce(
        productId: String,
        socialOrderId: String,
        purchaseToken: String,
        force: Boolean = false,
    ): Boolean {
        // orderId 가 null 인 구매(프로모·테스트)는 socialOrderId 가 "-" 라 서버가 식별할 수 없다
        if (socialOrderId == "-") return false

        val isNew = consumeAttemptedTokens.add(purchaseToken)
        if (!isNew && !force) return false

        consumeProductOrderUseCase(
            command = ConsumeProductOrderUseCase.Command(
                productId = productId,
                socialOrderId = socialOrderId,
                purchaseToken = purchaseToken,
            )
        )

        return true
    }

    /**
     * 미소비 주문 자동 소비
     *
     * 호출 지점은 init 과 onResume 뿐이다. initialize() 나 exceptionHandler 에 넣으면
     * 소비 실패 -> exceptionHandler -> initialize() -> 소비 실패 로 무한 루프가 된다
     * (BaseViewModel 의 핸들러는 별도 CoroutineScope 라 VM clear 이후에도 돈다).
     * 자동 소비가 실패하면 initialize() 가 상품을 다시 그려 "소비" 버튼이 수동 폴백으로 남는다.
     */
    private suspend fun autoConsume() {
        val orderVos = _productVos.value
            .flatMap { it.orderVos }
            .distinctBy { it.purchaseToken }

        var consumedAny = false

        orderVos.forEach { orderVo ->
            val consumed = consumeOnce(
                productId = orderVo.productId,
                socialOrderId = orderVo.socialOrderId,
                purchaseToken = orderVo.purchaseToken,
            )

            if (consumed) {
                consumedAny = true
            }
        }

        if (!consumedAny) return

        _productVos.value = getProductsUseCase()
        _uiEvent.emit(UiEvent.Buy("충전 완료"))
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            try {
                withContext(Dispatchers.IO) {
                    _productVos.value = getProductsUseCase()
                }
            } finally {
                _uiState.value = UiState.Idle
            }
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        when (exception) {
            is NotFoundPlayerException -> _uiEvent.emit(UiEvent.NavMain("잠시후 다시 시도"))
            is BillingNotSupportException -> _uiEvent.emit(UiEvent.NavMain("결제 미지원 기기"))
            else -> initialize()
        }
    }
}
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
                    consumeProductOrderUseCase(
                        command = ConsumeProductOrderUseCase.Command(
                            productId = googleOrderVo.productId,
                            socialOrderId = googleOrderVo.socialOrderId,
                            purchaseToken = googleOrderVo.purchaseToken,
                        )
                    )

                    _productVos.value = getProductsUseCase()
                    _uiEvent.emit(UiEvent.Buy("충전 완료"))
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
                    consumeProductOrderUseCase(
                        command = ConsumeProductOrderUseCase.Command(
                            productId = orderVo.productId,
                            socialOrderId = orderVo.socialOrderId,
                            purchaseToken = orderVo.purchaseToken,
                        )
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
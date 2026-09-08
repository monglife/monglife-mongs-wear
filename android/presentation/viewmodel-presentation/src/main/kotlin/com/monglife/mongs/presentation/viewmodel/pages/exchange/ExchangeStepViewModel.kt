package com.monglife.mongs.presentation.viewmodel.pages.exchange

import com.monglife.core.presentation.utils.PermissionUtil
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.device.exception.InvalidExchangeWalkingCountException
import com.monglife.mongs.application.device.usecase.ExchangeWalkingCountUseCase
import com.monglife.mongs.application.device.usecase.ObserveCurrentWalkingCountUseCase
import com.monglife.mongs.application.device.usecase.StartStepCollectionUseCase
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.device.model.StepExchangeRate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class ExchangeStepViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeCurrentWalkingCountUseCase: ObserveCurrentWalkingCountUseCase,
    private val startStepCollectionUseCase: StartStepCollectionUseCase,
    private val exchangeWalkingCountUseCase: ExchangeWalkingCountUseCase,
    private val permissionUtil: PermissionUtil,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val confirmDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Confirm : UiState(confirmDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class NavPopBackStack(val message: String): UiEvent()
        data class Exchange(val message: String): UiEvent()
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
    private val _permission = MutableStateFlow(false)
    val permission: StateFlow<Boolean> = _permission.asStateFlow()

    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    private val _walkingCount = MutableStateFlow(0)
    val walkingCount: StateFlow<Int> = _walkingCount.asStateFlow()

    /** 환전에 쓸 수 있는 확정 잔액. 실시간으로 얹은 걸음은 빠져 있다. */
    private val _exchangeableWalkingCount = MutableStateFlow(0)

    /** 지금 잔액으로 고를 수 있는 최대 환전 단위 수 */
    private val _maxExchangeUnits = MutableStateFlow(0)
    val maxExchangeUnits: StateFlow<Int> = _maxExchangeUnits.asStateFlow()

    private val _exchangeCount = MutableStateFlow(0)
    val exchangeCount: StateFlow<Int> = _exchangeCount.asStateFlow()

    private val _chargePayPoint = MutableStateFlow(0)
    val chargePayPoint: StateFlow<Int> = _chargePayPoint.asStateFlow()

    /** 지금 선택한 만큼 환전하고 나면 남는 걸음 수 */
    private val _remainingWalkingCount = MutableStateFlow(0)
    val remainingWalkingCount: StateFlow<Int> = _remainingWalkingCount.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                // 활동 권한 정보 목록
                _permission.value = permissionUtil.verifyActivityPermission().isEmpty()

                getCurrentMongUseCase()?.let {
                    _currentMongVo.value = it
                } ?: run {
                    _uiEvent.emit(UiEvent.NavPopBackStack("선택된 몽이 없음"))
                    return@withContext
                }

                observeForever(observeCurrentMongUseCase(), _currentMongVo)
                observeForever(observeCurrentWalkingCountUseCase()) { stepVo ->
                    _walkingCount.value = stepVo.walkingCount
                    _exchangeableWalkingCount.value = stepVo.exchangeableWalkingCount
                    _maxExchangeUnits.value =
                        StepExchangeRate.maxExchangeableUnits(stepVo.exchangeableWalkingCount)
                    // 걷는 중에 잔액이 바뀌면 이미 고른 수량이 잔액을 넘길 수 있다.
                    applyExchangeCount(min(_exchangeCount.value, _maxExchangeUnits.value))
                }
            }

            _uiState.value = UiState.Idle
        }
    }

    fun increaseExchangeCount() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            applyExchangeCount(min(_exchangeCount.value + 1, _maxExchangeUnits.value))
        }
    }

    fun decreaseExchangeCount() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            applyExchangeCount(max(_exchangeCount.value - 1, 0))
        }
    }

    /**
     * 환전 확인 다이얼로그 오픈
     */
    fun exchangeConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Confirm
        }
    }

    /**
     * 환전 확인 다이얼로그 닫기
     */
    fun exchangeConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 스타 포인트 환전
     */
    fun exchange(mongId: Long, exchangeCount: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                exchangeWalkingCountUseCase(
                    command = ExchangeWalkingCountUseCase.Command(
                        mongId = mongId,
                        exchangeUnits = exchangeCount,
                    )
                )
                applyExchangeCount(0)
            }

            _uiEvent.emit(UiEvent.Exchange(message = "환전 완료"))
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 활동 권한 체크
     */
    fun verifyActivityPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            val granted = permissionUtil.verifyActivityPermission().isEmpty()
            _permission.value = granted

            // 권한이 없으면 수집 경로가 NONE 으로 내려가 있다. 방금 허용했다면 다시 해석해
            // 곧바로 걸음을 세기 시작하게 한다. 멱등이라 이미 수집 중이어도 무해하다.
            if (granted) startStepCollectionUseCase()
        }
    }

    /**
     * 선택 수량과 거기서 파생되는 표시값을 한꺼번에 맞춘다.
     * 지급 payPoint 와 남는 걸음 수가 선택 수량과 어긋나지 않게 한곳에서만 계산한다.
     */
    private fun applyExchangeCount(units: Int) {
        val safeUnits = units.coerceIn(0, _maxExchangeUnits.value)
        _exchangeCount.value = safeUnits
        _chargePayPoint.value = StepExchangeRate.payPointOf(safeUnits)
        _remainingWalkingCount.value =
            max(0, _walkingCount.value - StepExchangeRate.walkingCountOf(safeUnits))
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        when (exception) {
            is NotFoundMongException -> _uiEvent.emit(UiEvent.NavPopBackStack("잠시후 다시 시도"))
            is InvalidExchangeWalkingCountException -> _uiState.value = UiState.Idle
            else -> initialize()
        }
    }
}

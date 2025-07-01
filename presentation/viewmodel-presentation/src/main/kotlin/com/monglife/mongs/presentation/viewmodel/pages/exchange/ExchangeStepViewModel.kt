package com.monglife.mongs.presentation.viewmodel.pages.exchange

import com.monglife.mongs.application.device.usecase.ExchangeWalkingCountUseCase
import com.monglife.mongs.application.device.usecase.ObserveCurrentWalkingCountUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.presentation.utils.PermissionUtil
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
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

@HiltViewModel
class ExchangeStepViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeCurrentWalkingCountUseCase: ObserveCurrentWalkingCountUseCase,
    private val exchangeWalkingCountUseCase: ExchangeWalkingCountUseCase,
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
        data class NavMenu(val message: String): UiEvent()
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
    private val _activityPermission = MutableStateFlow(false)
    val activityPermission: StateFlow<Boolean> = _activityPermission.asStateFlow()

    private val _mongVo = MutableStateFlow<MongVo?>(null)
    val mongVo: StateFlow<MongVo?> = _mongVo.asStateFlow()

    private val _walkingCount = MutableStateFlow(0)
    val walkingCount: StateFlow<Int> = _walkingCount.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                // 활동 권한 정보 목록
                _activityPermission.value = permissionUtil.verifyActivityPermission().isEmpty()

                observeForever(observeCurrentMongUseCase(), _mongVo)

                _mongVo.value ?: run {
                    _uiEvent.emit(UiEvent.NavMenu("선택된 몽이 없음"))
                    return@withContext
                }

                observeForever(observeCurrentWalkingCountUseCase(), _walkingCount)
            }

            _uiState.value = UiState.Idle
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
    fun exchange(mongId: Long, walkingCount: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                exchangeWalkingCountUseCase(
                    command = ExchangeWalkingCountUseCase.Command(
                        mongId = mongId,
                        walkingCount = walkingCount,
                    )
                )
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
            _activityPermission.value = permissionUtil.verifyActivityPermission().isEmpty()
        }
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
        initialize()
    }
}
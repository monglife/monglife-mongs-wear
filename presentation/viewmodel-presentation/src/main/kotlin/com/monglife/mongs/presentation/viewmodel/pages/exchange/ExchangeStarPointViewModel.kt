package com.monglife.mongs.presentation.viewmodel.pages.exchange

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.member.player.usecase.ExchangeStarPointUseCase
import com.monglife.mongs.application.member.player.usecase.ObservePlayerUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExchangeStarPointViewModel @Inject constructor(
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeStarPointUseCase: ObservePlayerUseCase,
    private val exchangeStarPointUseCase: ExchangeStarPointUseCase,
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
    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    private val _starPoint = MutableStateFlow(0)
    val starPoint: StateFlow<Int> = _starPoint.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                observeCurrentMongUseCase()
                    .shareIn(viewModelScopeWithHandler, SharingStarted.Eagerly, replay = 1)
                    .let { flow -> observeForever(flow, _currentMongVo) }

                _currentMongVo.value ?: run {
                    _uiEvent.emit(UiEvent.NavMenu("선택된 몽이 없음"))
                    return@withContext
                }

                observeStarPointUseCase()
                    .shareIn(viewModelScopeWithHandler, SharingStarted.Eagerly, replay = 1)
                    .let { flow -> observeForever(flow.map { it.starPoint }, _starPoint) }
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
    fun exchange(mongId: Long, starPoint: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                exchangeStarPointUseCase(
                    command = ExchangeStarPointUseCase.Command(
                        mongId = mongId,
                        starPoint = starPoint,
                    )
                )
            }

            _uiEvent.emit(UiEvent.Exchange(message = "환전 완료"))
            _uiState.value = UiState.Idle
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
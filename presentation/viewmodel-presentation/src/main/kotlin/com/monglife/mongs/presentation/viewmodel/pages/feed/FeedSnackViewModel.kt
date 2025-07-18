package com.monglife.mongs.presentation.viewmodel.pages.feed

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.usecase.interaction.FeedSnackMongUseCase
import com.monglife.mongs.application.mong.usecase.interaction.GetSnacksUseCase
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.application.mong.vo.SnackVo
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
class FeedSnackViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val getSnacksUseCase: GetSnacksUseCase,
    private val feedSnackMongUseCase: FeedSnackMongUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val detailDialogOpen: Boolean = false,
        val confirmDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Detail: UiState(detailDialogOpen = true)
        data object Confirm: UiState(confirmDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class NavMenu(val message: String): UiEvent()
        data object Buy: UiEvent()
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

    private val _snackVos = MutableStateFlow<List<SnackVo>>(emptyList())
    val snackVos: StateFlow<List<SnackVo>> = _snackVos.asStateFlow()

    private val _currentSnackVo = MutableStateFlow<SnackVo?>(null)
    val currentSnackVo: StateFlow<SnackVo?> = _currentSnackVo.asStateFlow()

    private val _snackVoIndex = MutableStateFlow(0)
    val snackVoIndex: StateFlow<Int> = _snackVoIndex.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getCurrentMongUseCase()?.let {
                    _currentMongVo.value = it

                    // 간식 목록 조회
                    _snackVos.value = getSnacksUseCase(
                        command = GetSnacksUseCase.Command(
                            mongId = it.mongId,
                        )
                    )

                    if (_snackVoIndex.value in 0..< _snackVos.value.size) {
                        _currentSnackVo.value = _snackVos.value[_snackVoIndex.value]
                    } else {
                        _currentSnackVo.value = null
                    }

                } ?: _uiEvent.emit(UiEvent.NavMenu("선택된 몽이 없음"))

                observeForever(observeCurrentMongUseCase(), _currentMongVo)
            }

            _uiState.value = UiState.Idle
        }
    }

    fun nextSnack() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _snackVoIndex.value = min(_snackVoIndex.value + 1, _snackVos.value.size - 1)

            if (_snackVoIndex.value in 0..< _snackVos.value.size) {
                _currentSnackVo.value = _snackVos.value[_snackVoIndex.value]
            } else {
                _currentSnackVo.value = null
            }
        }
    }

    fun prevSnack() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _snackVoIndex.value = max(_snackVoIndex.value - 1, 0)

            if (_snackVoIndex.value in 0..< _snackVos.value.size) {
                _currentSnackVo.value = _snackVos.value[_snackVoIndex.value]
            } else {
                _currentSnackVo.value = null
            }
        }
    }

    /**
     * 먹이 섭취
     */
    fun buy(mongId: Long, snackCode: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                feedSnackMongUseCase(
                    command = FeedSnackMongUseCase.Command(
                        mongId = mongId,
                        snackCode = snackCode,
                    )
                )

                _uiEvent.emit(UiEvent.Buy)
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 상세 다이얼로그 오픈
     */
    fun detailDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Detail
        }
    }

    /**
     * 상세 다이얼로그 닫기
     */
    fun detailDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 구매 확인 다이얼로그 오픈
     */
    fun buyConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Confirm
        }
    }

    /**
     * 구매 확인 다이얼로그 닫기
     */
    fun buyConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
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
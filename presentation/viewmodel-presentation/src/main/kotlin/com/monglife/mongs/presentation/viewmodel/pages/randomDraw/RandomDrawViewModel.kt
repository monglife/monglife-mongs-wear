package com.monglife.mongs.presentation.viewmodel.pages.randomDraw

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.usecase.interaction.RandomDrawUseCase
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.application.mong.vo.RandomDrawVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
class RandomDrawViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val randomDrawUseCase: RandomDrawUseCase,
): BaseViewModel() {

    companion object {
        private const val DRAW_DELAY = 2000L
        private const val DRAW_PAY_POINT = 100
    }

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val confirmDialogOpen: Boolean = false,
        val enteringDialogOpen: Boolean = false,
        val drawLoading: Boolean = false,
        val randomDrawDetailDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Entering : UiState(enteringDialogOpen = true)
        data object Confirm : UiState(confirmDialogOpen = true)
        data object Draw : UiState(drawLoading = true)
        data object Detail : UiState(randomDrawDetailDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
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
    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    private val _randomDrawVo = MutableStateFlow<RandomDrawVo?>(null)
    val randomDrawVo: StateFlow<RandomDrawVo?> = _randomDrawVo.asStateFlow()

    private val _randomDrawPayPoint = MutableStateFlow(0)
    val randomDrawPayPoint: StateFlow<Int> = _randomDrawPayPoint.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getCurrentMongUseCase()?.let {
                    _currentMongVo.value = it
                } ?: run {
                    _uiEvent.emit(UiEvent.NavMain("선택된 몽이 없음"))
                    return@withContext
                }

                _randomDrawPayPoint.value = DRAW_PAY_POINT
            }

            _uiState.value = UiState.Entering
        }
    }

    /**
     * 랜덤 뽑기
     */
    fun randomDraw(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Draw

            delay(DRAW_DELAY)

            _randomDrawVo.value = randomDrawUseCase(
                command = RandomDrawUseCase.Command(
                    mongId = mongId,
                )
            )

            _currentMongVo.value = getCurrentMongUseCase()

            _uiState.value = UiState.Detail
        }
    }

    /**
     * 랜덤 뽑기 다이얼로그 오픈
     */
    fun randomDrawConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Confirm
        }
    }

    /**
     * 랜덤 뽑기 다이얼로그 닫기
     */
    fun randomDrawConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Entering
        }
    }

    /**
     * 랜덤 뽑기 결과 다이얼로그 닫기
     */
    fun randomDrawDetailDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Entering
            _randomDrawVo.value = null
        }
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Entering
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        when (exception) {
            is NotFoundMongException -> _uiEvent.emit(UiEvent.NavMain("잠시후 다시 시도"))
            else -> initialize()
        }
    }
}
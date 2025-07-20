package com.monglife.mongs.presentation.viewmodel.pages.training

import com.monglife.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingPlayViewModel @Inject constructor(

): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val enteringLoadingBar: Boolean = false,
        val runnerContent: Boolean = false,
        val basketballContent: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Entering : UiState(enteringLoadingBar = true)
        data object Runner : UiState(runnerContent = true)
        data object Basketball : UiState(basketballContent = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle : UiEvent()
        data class NavMenu(val message: String): UiEvent()
    }

    /**
     * UI 이벤트 변수
     */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Entering
        }
    }

    /**
     * 훈련 입장
     */
    fun enter(trainingCode: String?) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            trainingCode?.let {
                _uiState.value = when (it) {
                    "TR000" -> UiState.Runner
                    "TR001" -> UiState.Basketball
                    else -> UiState.Idle
                }
            } ?: run {
                _uiEvent.emit(UiEvent.NavMenu("훈련 입장 실패"))
            }
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
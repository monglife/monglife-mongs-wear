package com.monglife.mongs.presentation.viewmodel.pages.training.basketball

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.usecase.activity.GetTrainingUseCase
import com.monglife.mongs.application.mong.usecase.activity.TrainingEndUseCase
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.application.mong.vo.TrainingEndVo
import com.monglife.mongs.application.mong.vo.TrainingTypeVo
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.engine.BasketballEngine
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.vo.BasketballVo
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
class TrainingBasketballViewModel @Inject constructor(
    private val getTrainingUseCase: GetTrainingUseCase,
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val trainingEndUseCase: TrainingEndUseCase,
    private val basketballEngine: BasketballEngine,
): BaseViewModel() {

    companion object {
        private const val END_DELAY = 600L
    }

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val enteringDialog: Boolean = false,
        val playSection: Boolean = false,
        val stopSection: Boolean = false,
        val endDialog: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Entering: UiState(enteringDialog = true)
        data object Running: UiState(playSection = true)
        data object Stop: UiState(playSection = true, stopSection = true)
        data object End: UiState(endDialog = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle : UiEvent()
        data class NavMenu(val message: String = ""): UiEvent()
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

    /**
     * 변수
     */
    private val _trainingTypeVo = MutableStateFlow<TrainingTypeVo?>(null)
    val trainingTypeVo: StateFlow<TrainingTypeVo?> = _trainingTypeVo.asStateFlow()

    private val _trainingEndVo = MutableStateFlow<TrainingEndVo?>(null)
    val trainingEndVo: StateFlow<TrainingEndVo?> = _trainingEndVo.asStateFlow()

    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    private val _basketballVo = MutableStateFlow<BasketballVo?>(null)
    val basketballVo: StateFlow<BasketballVo?> = _basketballVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {

                getCurrentMongUseCase()?.let {
                    _currentMongVo.value = it
                } ?: run {
                    _uiEvent.emit(UiEvent.NavMenu("선택된 몽이 없음"))
                    return@withContext
                }

                observeForever(observeCurrentMongUseCase(), _currentMongVo)
            }
        }
    }

    /**
     * 입장 (초기 설정)
     */
    fun enter(
        trainingCode: String?,
        ballInitY: Float,
        ballInitX: Float,
        basketInitY: Float,
        basketInitX: Float,
    ) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            if (trainingCode == null) {
                _uiEvent.emit(UiEvent.NavMenu("훈련 입장 실패"))
                return@launch
            }

            withContext(Dispatchers.IO) {
                _trainingTypeVo.value = getTrainingUseCase(
                    command = GetTrainingUseCase.Command(
                        trainingCode = trainingCode
                    )
                )

                // 게임 엔진 초기 설정
                _basketballVo.value = basketballEngine.generate(
                    ballInitY = ballInitY,
                    ballInitX = ballInitX,
                    ballInitRadius = 50f,
                    basketHeight = 22f,
                    basketWidth = 125f,
                    basketInitY = basketInitY,
                    basketInitX = basketInitX,
                )
            }

            _uiState.value = UiState.Entering
        }
    }

    /**
     * 시작
     */
    fun start() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                _basketballVo.value?.let {
                    observeForever(basketballEngine.start(basketballId = it.basketballId), _basketballVo)
                }
            }

            _uiState.value = UiState.Running
        }
    }

    /**
     * 정지
     */
    fun stop() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            if (_uiState.value == UiState.Stop) return@launch

            withContext(Dispatchers.IO) {
                _basketballVo.value?.let {
                    basketballEngine.stop(basketballId = it.basketballId)
                }

                delay(END_DELAY)
            }

            _uiState.value = UiState.Stop
        }
    }

    /**
     * 종료
     */
    fun end(mongId: Long, trainingCode: String, score: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _trainingEndVo.value = trainingEndUseCase(
                    command = TrainingEndUseCase.Command(
                        mongId = mongId,
                        trainingCode = trainingCode,
                        score = score,
                    )
                )
            }

            _uiState.value = UiState.End
        }
    }

    /**
     * 공 던지기
     */
    fun throwBall(basketballId: String, vy: Float, vx: Float) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                basketballEngine.throwBall(
                    basketballId = basketballId,
                    vy = vy,
                    vx = vx,
                )
            }
        }
    }

    /**
     * 퇴장
     */
    fun exit() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiEvent.emit(UiEvent.NavMenu())
        }
    }

    override fun onCleared() {
        super.onCleared()

        _basketballVo.value?.let {
            basketballEngine.stop(basketballId = it.basketballId)
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
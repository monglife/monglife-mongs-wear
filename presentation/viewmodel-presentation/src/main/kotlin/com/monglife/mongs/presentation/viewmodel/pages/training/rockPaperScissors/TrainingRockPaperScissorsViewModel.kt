package com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.exception.InvalidTrainingException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.exception.NotFoundTrainingException
import com.monglife.mongs.application.mong.usecase.activity.GetTrainingUseCase
import com.monglife.mongs.application.mong.usecase.activity.TrainingEndUseCase
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.application.mong.vo.TrainingEndVo
import com.monglife.mongs.application.mong.vo.TrainingTypeVo
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.enums.RockPaperScissorsPickCode
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.vo.RockPaperScissorsVo
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
class TrainingRockPaperScissorsViewModel @Inject constructor(
    private val getTrainingUseCase: GetTrainingUseCase,
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val trainingEndUseCase: TrainingEndUseCase,
) : BaseViewModel() {

    companion object {
        private const val END_DELAY = 1800L
    }

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val enteringDialog: Boolean = false,
        val pickDialog: Boolean = false,
        val pickEndLoadingBar: Boolean = false,
        val playSection: Boolean = false,
        val endDialog: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Entering: UiState(enteringDialog = true)
        data object Pick: UiState(pickDialog = true)
        data object PickEnd: UiState(pickEndLoadingBar = true)
        data object Play: UiState(playSection = true)
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

    private val _rockPaperScissorsVo = MutableStateFlow<RockPaperScissorsVo?>(null)
    val rockPaperScissorsVo: StateFlow<RockPaperScissorsVo?> = _rockPaperScissorsVo.asStateFlow()

    private val _isStart = MutableStateFlow(false)
    val isStart: StateFlow<Boolean> = _isStart.asStateFlow()

    private val _isProcess = MutableStateFlow(false)
    val isProcess: StateFlow<Boolean> = _isProcess.asStateFlow()

    private val _timeMillis = MutableStateFlow(0F)
    val timeMillis: StateFlow<Float> = _timeMillis.asStateFlow()

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
    fun enter(trainingCode: String?) {
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
                // 게임 엔진 초기 설정
                _isStart.value = true
                _isProcess.value = true
                _timeMillis.value = 0F

                _rockPaperScissorsVo.value = RockPaperScissorsVo(
                    randomPickCode = null,
                    pickCode = null,
                    result = 0,
                    score = 0,
                )

                _uiState.value = UiState.Pick

                // 타이머 실행
                while (_isProcess.value) {
                    delay(100)
                    _timeMillis.value += 100F
                }
            }
        }
    }

    /**
     * 선택
     */
    fun pick(pickCode: RockPaperScissorsPickCode) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            _uiState.value = UiState.PickEnd

            val randomPickCode = RockPaperScissorsPickCode.entries.toTypedArray().random()

            val result = if (randomPickCode == pickCode) {
                0
            } else if (randomPickCode == RockPaperScissorsPickCode.ROCK) {
                if (pickCode == RockPaperScissorsPickCode.PAPER) 1 else -1
            } else if (randomPickCode == RockPaperScissorsPickCode.PAPER) {
                if (pickCode == RockPaperScissorsPickCode.SCISSORS) 1 else -1
            } else {
                if (pickCode == RockPaperScissorsPickCode.ROCK) 1 else -1
            }

            _rockPaperScissorsVo.value = RockPaperScissorsVo(
                randomPickCode = randomPickCode,
                pickCode = pickCode,
                result = result,
                score = _rockPaperScissorsVo.value?.score ?: 0,
            )

            _uiState.value = UiState.Play
        }
    }

    /**
     * 결과
     */
    fun over() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _rockPaperScissorsVo.value?.let {
                _rockPaperScissorsVo.value = RockPaperScissorsVo(
                    randomPickCode = it.randomPickCode,
                    pickCode = it.pickCode,
                    result = it.result,
                    score = it.score + if (it.result == 1) 1 else 0
                )
            }

            delay(END_DELAY)

            if (_isProcess.value) {
                _uiState.value = UiState.Pick
            }
        }
    }

    /**
     * 정지
     */
    fun stop() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                _isProcess.value = false
            }
        }
    }

    /**
     * 종료
     */
    fun end(mongId: Long, trainingCode: String, score: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            delay(END_DELAY)

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
     * 퇴장
     */
    fun exit() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiEvent.emit(UiEvent.NavMenu())
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
            is NotFoundMongException -> _uiEvent.emit(UiEvent.NavMenu("잠시후 다시 시도"))
            is NotFoundTrainingException -> _uiEvent.emit(UiEvent.NavMenu("잠시후 다시 시도"))
            is InvalidTrainingException -> _uiState.value = UiState.Entering
            else -> initialize()
        }
    }
}
package com.mongs.wear.presentation.pages.training.basketball

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mongs.wear.core.enums.TrainingCode
import com.mongs.wear.core.exception.usecase.GetTrainingUseCaseException
import com.mongs.wear.core.exception.usecase.TrainingEndUseCaseException
import com.mongs.wear.domain.management.usecase.GetCurrentSlotUseCase
import com.mongs.wear.domain.management.vo.MongVo
import com.mongs.wear.domain.training.usecase.GetTrainingUseCase
import com.mongs.wear.domain.training.usecase.TrainingEndUseCase
import com.mongs.wear.presentation.global.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TrainingBasketballViewModel  @Inject constructor(
    private val getTrainingUseCase: GetTrainingUseCase,
    private val getCurrentSlotUseCase: GetCurrentSlotUseCase,
    private val trainingEndUseCase: TrainingEndUseCase,
) : BaseViewModel() {

    companion object {
        private const val DEFAULT_BALL_RADIUS = 50f
        private const val DEFAULT_BASKET_WIDTH = 115f
        private const val DEFAULT_BASKET_HEIGHT = 22f
    }

    val basketballEngine = BasketballEngine()

    private val _mongVo = MediatorLiveData<MongVo?>(null)
    val mongVo: LiveData<MongVo?> get() = _mongVo

    private val _trainingPayPoint = MediatorLiveData(0)
    val trainingPayPoint: LiveData<Int> get() = _trainingPayPoint

    private val _trainingScore = MediatorLiveData(0)
    val trainingScore: LiveData<Int> get() = _trainingScore

    private val _time = MediatorLiveData(0)
    val time: LiveData<Int> get() = _time

    private val _timeout = MediatorLiveData(0)
    val timeout: LiveData<Int> get() = _timeout

    private val _isSuccess = MediatorLiveData(false)
    val isSuccess: LiveData<Boolean> get() = _isSuccess

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            uiState.loadingBar = true

            val trainingVo = getTrainingUseCase(
                GetTrainingUseCase.Param(
                    trainingCode = TrainingCode.BASKETBALL,
                )
            )

            // 보상 페이 포인트
            _trainingPayPoint.postValue(trainingVo.rewardPayPoint)

            // 성공 조건 스코어
            _trainingScore.postValue(trainingVo.score)

            // 게임 플레이 시간
            _timeout.postValue(trainingVo.timeout)

            _mongVo.addSource(withContext(Dispatchers.IO) { getCurrentSlotUseCase() }) { mongVo ->
                _mongVo.value = mongVo
            }

            uiState.loadingBar = false
        }
    }

    /**
     * 훈련 농구 시작
     */
    fun basketballStart(timeout: Int, ballSx: Float, ballSy: Float, basketSx: Float, basketSy: Float) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {

            uiState.trainingStartDialog = false
            uiState.trainingOverDialog = false

            basketballEngine.start(
                ballSr = DEFAULT_BALL_RADIUS,
                ballSx = ballSx,
                ballSy = ballSy,
                basketSx = basketSx,
                basketSy = basketSy,
                basketWidth = DEFAULT_BASKET_WIDTH,
                basketHeight = DEFAULT_BASKET_HEIGHT,
            )

            var currentTime = timeout
            _time.postValue(currentTime)

            while (basketballEngine.isStartGame.value && currentTime > 0) {
                delay(1000)
                currentTime -= 1
                _time.postValue(currentTime)
            }

            basketballEngine.end()
        }
    }

    /**
     * 훈련 농구 종료
     */
    fun basketballEnd(mongId: Long, score: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            basketballEngine.end()

            val trainingEndVo = trainingEndUseCase(
                TrainingEndUseCase.Param(
                    mongId =  mongId,
                    score = score,
                    trainingCode = TrainingCode.BASKETBALL,
                )
            )
            // 훈련 성공 여부 판별
            _isSuccess.postValue(trainingEndVo.isSuccess)

            // 보상 페이 포인트
            _trainingPayPoint.postValue(trainingEndVo.rewardPayPoint)

            // 달성 스코어
            _trainingScore.postValue(trainingEndVo.score)

            uiState.trainingOverDialog = true
        }
    }

    /**
     * ViewModel 소멸자
     */
    override fun onCleared() {
        super.onCleared()
        basketballEngine.end()
    }

    val uiState = UiState()

    class UiState : BaseUiState() {
        var navMainEvent = MutableSharedFlow<Long>()
        var trainingStartDialog by mutableStateOf(true)
        var trainingOverDialog by mutableStateOf(false)
    }


    override suspend fun exceptionHandler(exception: Throwable) {
        when (exception) {
            is GetTrainingUseCaseException -> {
                uiState.navMainEvent.emit(System.currentTimeMillis())
            }

            is TrainingEndUseCaseException -> {
                // 훈련 성공 여부 판별
                _isSuccess.postValue(false)
                // 보상 페이 포인트
                _trainingPayPoint.postValue(0)
                // 달성 스코어
                _trainingScore.postValue(0)

                uiState.trainingOverDialog = true
            }

            else -> {
                uiState.loadingBar = false
            }
        }
    }
}
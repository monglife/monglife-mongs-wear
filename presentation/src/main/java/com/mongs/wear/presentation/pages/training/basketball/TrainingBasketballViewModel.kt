package com.mongs.wear.presentation.pages.training.basketball

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mongs.wear.core.enums.TrainingCode
import com.mongs.wear.domain.management.usecase.GetCurrentSlotUseCase
import com.mongs.wear.domain.management.vo.MongVo
import com.mongs.wear.domain.training.usecase.GetTrainingPayPointUseCase
import com.mongs.wear.presentation.global.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TrainingBasketballViewModel  @Inject constructor(
    private val getTrainingPayPointUseCase: GetTrainingPayPointUseCase,
    private val getCurrentSlotUseCase: GetCurrentSlotUseCase,
) : BaseViewModel() {

    companion object {
        private val DEFAULT_BALL_RADIUS = 50f
    }

    val basketballEngine = BasketballEngine()

    private val _mongVo = MediatorLiveData<MongVo?>(null)
    val mongVo: LiveData<MongVo?> get() = _mongVo

    private val _trainingPayPoint = MediatorLiveData<Int>(0)
    val trainingPayPoint: LiveData<Int> get() = _trainingPayPoint

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            uiState.loadingBar = true

            _trainingPayPoint.postValue(
                getTrainingPayPointUseCase(
                    GetTrainingPayPointUseCase.Param(
                        trainingCode = TrainingCode.RUNNER,
                    )
                )
            )

            _mongVo.addSource(withContext(Dispatchers.IO) { getCurrentSlotUseCase() }) { mongVo ->
                _mongVo.value = mongVo
            }

            uiState.loadingBar = false
        }
    }

    /**
     * 훈련 농구 시작
     */
    fun basketballStart(sx: Float, sy: Float) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            basketballEngine.endEvent.collect {
                Log.i("TEST", "game end")
            }
        }

        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            basketballEngine.start(sr = DEFAULT_BALL_RADIUS, sx = sx, sy = sy)
        }
    }

    /**
     * 훈련 농구 종료
     */
    fun basketballEnd() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            basketballEngine.end()

            // TODO: 훈련 종료 처리

            uiState.navMainEvent.emit(System.currentTimeMillis())
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
    }


    override suspend fun exceptionHandler(exception: Throwable) {
        when (exception) {
            else -> {
                uiState.loadingBar = false
            }
        }
    }
}
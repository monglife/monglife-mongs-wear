package com.mongs.wear.viewModel

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mongs.wear.core.exception.usecase.ConnectMqttUseCaseException
import com.mongs.wear.domain.device.usecase.SetNetworkUseCase
import com.mongs.wear.domain.device.usecase.SetServerTotalWalkingCountUseCase
import com.mongs.wear.domain.global.usecase.ConnectMqttUseCase
import com.mongs.wear.domain.global.usecase.DisConnectMqttUseCase
import com.mongs.wear.domain.global.usecase.PauseConnectMqttUseCase
import com.mongs.wear.domain.management.usecase.UpdateCurrentSlotUseCase
import com.mongs.wear.domain.player.usecase.UpdatePlayerUseCase
import com.mongs.wear.presentation.global.manager.StepSensorManager
import com.mongs.wear.presentation.global.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("HardwareIds")
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val stepSensorManager: StepSensorManager,
    private val setNetworkUseCase: SetNetworkUseCase,
    private val updateCurrentSlotUseCase: UpdateCurrentSlotUseCase,
    private val updatePlayerUseCase: UpdatePlayerUseCase,
    private val setServerTotalWalkingCountUseCase: SetServerTotalWalkingCountUseCase,
    private val connectMqttUseCase: ConnectMqttUseCase,
    private val pauseConnectMqttUseCase: PauseConnectMqttUseCase,
    private val disConnectMqttUseCase: DisConnectMqttUseCase,
) : BaseViewModel() {

    /**
     * 앱 초기화
     */
    fun initApp() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState.initPending = true

            // 네트워크 플래그 초기화
            setNetworkUseCase(SetNetworkUseCase.Param(network = false))
            // Mqtt 연결
            connectMqttUseCase()
            // 플레이어 정보 동기화
            updatePlayerUseCase()
            // 현재 몽 정보 동기화
            updateCurrentSlotUseCase()
            // 걸음 수 동기화
            setServerTotalWalkingCountUseCase(
                SetServerTotalWalkingCountUseCase.Param(
                    totalWalkingCount = stepSensorManager.getWalkingCount()
                )
            )

            uiState.initPending = false
        }
    }

    /**
     * 앱 서버 동기화
     */
    fun resumeApp() {
        if (uiState.initPending) return

        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            // Mqtt 연결
            connectMqttUseCase()
            // 플레이어 정보 동기화
            updatePlayerUseCase()
            // 현재 몽 정보 동기화
            updateCurrentSlotUseCase()
            // 걸음 수 동기화
            setServerTotalWalkingCountUseCase(
                SetServerTotalWalkingCountUseCase.Param(
                    totalWalkingCount = stepSensorManager.getWalkingCount()
                )
            )
        }
    }

    /**
     * 브로커 일시 중지
     */
    fun pauseMqtt() = viewModelScopeWithHandler.launch(Dispatchers.IO) {
        pauseConnectMqttUseCase()
    }

    /**
     * 브로커 연결 해제
     */
    fun disConnectMqtt() = viewModelScopeWithHandler.launch(Dispatchers.IO) {
        disConnectMqttUseCase()
    }

    val uiState = UiState()

    class UiState : BaseUiState() {
        var initPending by mutableStateOf(false)
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        exception.printStackTrace()
        when(exception) {
            is ConnectMqttUseCaseException -> {
                setNetworkUseCase(SetNetworkUseCase.Param(network = false))
            }

            else -> uiState.initPending = false
        }
    }
}
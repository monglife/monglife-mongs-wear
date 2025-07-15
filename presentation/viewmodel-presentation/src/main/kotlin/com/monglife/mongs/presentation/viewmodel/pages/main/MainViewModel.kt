package com.monglife.mongs.presentation.viewmodel.pages.main

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.device.usecase.ObserveBackgroundMapCodeUseCase
import com.monglife.mongs.application.device.usecase.SyncRemoteStepUseCase
import com.monglife.mongs.application.member.player.usecase.SyncRemotePlayerUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.SyncRemoteMongsUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val syncRemotePlayerUseCase: SyncRemotePlayerUseCase,
    private val syncRemoteStepUseCase: SyncRemoteStepUseCase,
    private val syncRemoteMongsUseCase: SyncRemoteMongsUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeBackgroundMapCodeUseCase: ObserveBackgroundMapCodeUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    private val _backgroundMapCode = MutableStateFlow<String?>(null)
    val backgroundMapCode: StateFlow<String?> = _backgroundMapCode.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                // 플레이어 정보 동기화
                syncRemotePlayerUseCase()
                // 몽 목록 서버 동기화
                syncRemoteMongsUseCase()
                // 걸음 수 서버 동기화
                syncRemoteStepUseCase()

                observeForever(observeCurrentMongUseCase(), _currentMongVo)
                observeForever(observeBackgroundMapCodeUseCase(), _backgroundMapCode)
            }

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

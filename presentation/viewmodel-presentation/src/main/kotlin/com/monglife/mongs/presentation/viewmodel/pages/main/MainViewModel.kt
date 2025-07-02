package com.monglife.mongs.presentation.viewmodel.pages.main

import com.monglife.mongs.application.device.usecase.ObserveBackgroundMapCodeUseCase
import com.monglife.mongs.application.device.usecase.SyncRemoteStepUseCase
import com.monglife.mongs.application.member.player.usecase.SyncRemotePlayerUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.SyncRemoteMongsUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
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
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _mongVo = MutableStateFlow<MongVo?>(null)
    val mongVo: StateFlow<MongVo?> = _mongVo.asStateFlow()

    private val _backgroundMapCode = MutableStateFlow<String?>(null)
    val backgroundMapCode: StateFlow<String?> = _backgroundMapCode.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                syncRemotePlayerUseCase()
                syncRemoteMongsUseCase()
                syncRemoteStepUseCase()

                observeCurrentMongUseCase()
                    .shareIn(viewModelScopeWithHandler, SharingStarted.Eagerly, replay = 1)
                    .let { flow -> observeForever(flow, _mongVo) }

                observeBackgroundMapCodeUseCase()
                    .shareIn(viewModelScopeWithHandler, SharingStarted.Eagerly, replay = 1)
                    .let { flow -> observeForever(flow, _backgroundMapCode) }
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

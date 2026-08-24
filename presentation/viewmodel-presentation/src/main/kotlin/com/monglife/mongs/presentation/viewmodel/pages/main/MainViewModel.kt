package com.monglife.mongs.presentation.viewmodel.pages.main

import android.util.Log
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.device.usecase.ObserveBackgroundMapCodeUseCase
import com.monglife.mongs.application.device.usecase.StartStepCollectionUseCase
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
    private val startStepCollectionUseCase: StartStepCollectionUseCase,
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
                // 동기화가 실패해도 화면은 떠야 하므로 삼키되, 조용히 사라지지는 않게 남긴다.
                runCatching {
                    // 플레이어 정보 동기화
                    syncRemotePlayerUseCase()
                    // 몽 목록 정보 동기화
                    syncRemoteMongsUseCase()
                    // 걸음 수 수집 시작 (경로 해석 + 등록, 멱등)
                    startStepCollectionUseCase()
                }.onFailure {
                    Log.w(this::class.simpleName, "메인 진입 동기화 실패", it)
                }

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

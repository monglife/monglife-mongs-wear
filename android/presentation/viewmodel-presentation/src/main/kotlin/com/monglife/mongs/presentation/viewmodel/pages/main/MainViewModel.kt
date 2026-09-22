package com.monglife.mongs.presentation.viewmodel.pages.main

import android.util.Log
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.device.usecase.ObserveBackgroundMapCodeUseCase
import com.monglife.mongs.application.device.usecase.StartStepCollectionUseCase
import com.monglife.mongs.application.member.player.usecase.ObservePlayerUseCase
import com.monglife.mongs.application.member.player.usecase.SyncRemotePlayerUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.SyncRemoteMongsUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val syncRemotePlayerUseCase: SyncRemotePlayerUseCase,
    private val observePlayerUseCase: ObservePlayerUseCase,
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

    // 스타 포인트는 몽이 아니라 계정에 붙는다. 몽이 없거나 죽어도 그대로 표시한다.
    private val _starPoint = MutableStateFlow(0)
    val starPoint: StateFlow<Int> = _starPoint.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                // 걸음 수 수집 시작 (경로 해석 + 등록 + flush, 멱등)
                //
                // 서버 동기화와 같은 runCatching 에 두면 안 된다. 수집 시작은 DataStore 와
                // Health Services 만 건드리는 로컬 동작인데, 앞의 네트워크 호출이 던지는 순간
                // 여기까지 오지 못해 오프라인에서는 걸음이 아예 안 쌓인다.
                // 먼저 부르는 이유는 flush 가 앱 꺼진 동안의 걸음을 당겨 오기 때문이다. 빠를수록 좋다.
                runCatching {
                    startStepCollectionUseCase()
                }.onFailure {
                    Log.w(this::class.simpleName, "걸음 수집 시작 실패", it)
                }

                // 동기화가 실패해도 화면은 떠야 하므로 삼키되, 조용히 사라지지는 않게 남긴다.
                runCatching {
                    // 플레이어 정보 동기화
                    syncRemotePlayerUseCase()
                    // 몽 목록 정보 동기화
                    syncRemoteMongsUseCase()
                }.onFailure {
                    Log.w(this::class.simpleName, "메인 진입 동기화 실패", it)
                }

                observeForever(observeCurrentMongUseCase(), _currentMongVo)
                observeForever(observeBackgroundMapCodeUseCase(), _backgroundMapCode)
                observeForever(observePlayerUseCase().map { it.starPoint }, _starPoint)
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

package com.monglife.mongs.presentation.viewmodel.pages.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.monglife.mongs.application.device.usecase.ObserveBackgroundMapCodeUseCase
import com.monglife.mongs.application.member.player.usecase.SyncRemotePlayerUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val syncRemotePlayerUseCase: SyncRemotePlayerUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeBackgroundMapCodeUseCase: ObserveBackgroundMapCodeUseCase,
): BaseViewModel() {

    private val _mongVo = MediatorLiveData<MongVo?>(null)
    val mongVo: LiveData<MongVo?> get() = _mongVo

    private val _backgroundMapCode = MediatorLiveData<String>()
    val backgroundMapCode: LiveData<String> get() = _backgroundMapCode

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Loading

            withContext(Dispatchers.IO) {
                syncRemotePlayerUseCase()

                val currentMongFlow = observeCurrentMongUseCase()
                    .stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, null)

                val backgroundMapCodeFlow = observeBackgroundMapCodeUseCase()
                    .stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, "")

                _mongVo.postValue(currentMongFlow.first())
                _backgroundMapCode.postValue(backgroundMapCodeFlow.first())

                observeForever(currentMongFlow, _mongVo)
                observeForever(backgroundMapCodeFlow, _backgroundMapCode)
            }

            uiState = UiState.Idle
        }
    }

    /**
     * UI 상태 변수
     */
    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

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
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Idle
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        initialize()
    }
}

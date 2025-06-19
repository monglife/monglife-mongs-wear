package com.monglife.mongs.presentation.viewmodel.pages.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.monglife.mongs.application.device.usecase.ObserveBackgroundMapCodeUseCase
import com.monglife.mongs.application.mong.usecase.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
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

            _mongVo.addSource(withContext(Dispatchers.IO) { observeCurrentMongUseCase().asLiveData() }) {
                _mongVo.value = it
            }

            _backgroundMapCode.addSource(withContext(Dispatchers.IO) { observeBackgroundMapCodeUseCase().asLiveData() }) {
                _backgroundMapCode.value = it
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
        // UI 초기화
        uiState = UiState.Idle
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        initialize()
    }
}
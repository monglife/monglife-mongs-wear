package com.monglife.mongs.presentation.viewmodel.layout

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.auth.usecase.GetMustUpdateAppUseCase
import com.monglife.mongs.application.auth.usecase.ObserveIsLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LayoutViewModel @Inject constructor(
    private val getMustUpdateAppUseCase: GetMustUpdateAppUseCase,
    private val observeIsLoginUseCase: ObserveIsLoginUseCase,
) : BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val mustUpdateApp: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object NeedUpdate : UiState(mustUpdateApp = true)
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _isLogin = MutableStateFlow<Boolean?>(null)
    val isLogin: StateFlow<Boolean?> = _isLogin.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            // 로그인 여부 로딩
            withContext(Dispatchers.IO) {
                observeForever(observeIsLoginUseCase(), _isLogin)
            }

            // 앱 업데이트 체크
            _uiState.value = withContext(Dispatchers.IO) {
                if (getMustUpdateAppUseCase()) UiState.NeedUpdate else UiState.Idle
            }
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
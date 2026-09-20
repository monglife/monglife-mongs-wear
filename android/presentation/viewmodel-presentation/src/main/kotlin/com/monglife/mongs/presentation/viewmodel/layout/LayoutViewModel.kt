package com.monglife.mongs.presentation.viewmodel.layout

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.auth.usecase.AppEntryState
import com.monglife.mongs.application.auth.usecase.GetAppEntryStateUseCase
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
    private val getAppEntryStateUseCase: GetAppEntryStateUseCase,
    private val observeIsLoginUseCase: ObserveIsLoginUseCase,
) : BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val mustUpdateApp: Boolean = false,
        val underMaintenance: Boolean = false,
        val maintenanceMessage: String? = null,
        val maintenanceEndAt: String? = null,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object NeedUpdate : UiState(mustUpdateApp = true)
        data class Maintenance(val message: String?, val endAt: String?) : UiState(
            underMaintenance = true,
            maintenanceMessage = message,
            maintenanceEndAt = endAt,
        )
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

            // 앱 진입 체크 (강제 업데이트 · 서버 점검)
            _uiState.value = withContext(Dispatchers.IO) {
                when (val state = getAppEntryStateUseCase()) {
                    is AppEntryState.NeedUpdate -> UiState.NeedUpdate
                    is AppEntryState.Maintenance -> UiState.Maintenance(message = state.message, endAt = state.endAt)
                    is AppEntryState.Normal -> UiState.Idle
                }
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

package com.monglife.mongs.presentation.viewmodel.layout

import com.monglife.mongs.application.auth.usecase.GetIsLoginUseCase
import com.monglife.mongs.application.auth.usecase.GetMustUpdateAppUseCase
import com.monglife.mongs.core.presentation.utils.PermissionUtil
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.presentation.viewmodel.layout.LoginViewModel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LayoutViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
    private val getMustUpdateAppUseCase: GetMustUpdateAppUseCase,
    private val getIsLoginUseCase: GetIsLoginUseCase,
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
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class RequestPermission(val permissions: List<String>): UiEvent()
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * UI 이벤트 변수
     */
    private val _uiEvent = MutableStateFlow<UiEvent>(UiEvent.Idle)
    val uiEvent: StateFlow<UiEvent> = _uiEvent.asStateFlow()

    /**
     * 변수
     */
    private val _isLogin = MutableStateFlow(false)
    val isLogin: StateFlow<Boolean> = _isLogin.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            // 로그인 여부 로딩
            withContext(Dispatchers.IO) {
                getIsLoginUseCase()
                    .stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, false)
                    .let {
                        observeForever(it, _isLogin)
                        _isLogin.value = it.first()
                    }
            }

            // 앱 업데이트 체크
            val mustUpdateApp = runCatching {
                withContext(Dispatchers.IO) { getMustUpdateAppUseCase() }
            }.getOrElse { true }

            if (mustUpdateApp) {
                _uiState.value = UiState.NeedUpdate
                return@launch
            }

            // 권한 확인
            val permissions = buildList {
                addAll(permissionUtil.verifyNotificationPermission())
                addAll(permissionUtil.verifyActivityPermission())
                addAll(permissionUtil.verifyLocationPermission())
            }

            if (permissions.isNotEmpty()) {
                _uiEvent.emit(UiEvent.RequestPermission(permissions))
            } else {
                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 권한 부여 설정 Activity 종료
     */
    fun verifyPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
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
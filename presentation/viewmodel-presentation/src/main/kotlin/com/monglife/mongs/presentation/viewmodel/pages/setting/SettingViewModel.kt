package com.monglife.mongs.presentation.viewmodel.pages.setting

import com.monglife.mongs.application.auth.usecase.LogoutUseCase
import com.monglife.mongs.application.device.usecase.ObserveNotificationOptionUseCase
import com.monglife.mongs.application.device.usecase.SetNotificationOptionUseCase
import com.monglife.mongs.core.presentation.utils.PermissionUtil
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
    private val observeNotificationOptionUseCase: ObserveNotificationOptionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val setNotificationOptionUseCase: SetNotificationOptionUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val logoutDialogOpen: Boolean = false
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Logout : UiState(logoutDialogOpen = true)
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
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    /**
     * 변수
     */
    private val _notificationOption = MutableStateFlow(false)
    val notificationOption: StateFlow<Boolean> get() = _notificationOption

    private val _notificationPermission = MutableStateFlow(false)
    val notificationPermission: StateFlow<Boolean> get() = _notificationPermission

    private val _activityPermission = MutableStateFlow(false)
    val activityPermission: StateFlow<Boolean> get() = _activityPermission

    private val _locationPermission = MutableStateFlow(false)
    val locationPermission: StateFlow<Boolean> get() = _locationPermission

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                observeNotificationOptionUseCase()
                    .stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, false)
                    .let {
                        observeForever(it, _notificationOption)
                        _notificationOption.value = it.first()
                    }
                _notificationPermission.value = permissionUtil.verifyNotificationPermission().isEmpty()
                _activityPermission.value = permissionUtil.verifyActivityPermission().isEmpty()
                _locationPermission.value = permissionUtil.verifyLocationPermission().isEmpty()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 알림 옵션 설정 토글
     */
    fun toggleNotificationOption(notificationOption: Boolean) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            setNotificationOptionUseCase(
                command = SetNotificationOptionUseCase.Command(
                    notificationOption = !notificationOption
                )
            )
        }
    }

    /**
     * 로그아웃 다이얼로그 오픈
     */
    fun logoutDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Logout
        }
    }

    /**
     * 로그아웃 다이얼로그 닫기
     */
    fun logoutDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 로그아웃
     */
    fun logout() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                logoutUseCase()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 알림 권한 요청
     */
    fun requestNotificationPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                permissionUtil.verifyNotificationPermission()
            }.let { permissions ->
                _uiEvent.emit(UiEvent.RequestPermission(permissions = permissions))
            }
        }
    }

    /**
     * 활동 권한 요청
     */
    fun requestActivityPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                permissionUtil.verifyActivityPermission()
            }.let { permissions ->
                _uiEvent.emit(UiEvent.RequestPermission(permissions = permissions))
            }
        }
    }

    /**
     * 위치 권한 요청
     */
    fun requestLocationPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                permissionUtil.verifyLocationPermission()
            } .let { permissions ->
                _uiEvent.emit(UiEvent.RequestPermission(permissions = permissions))
            }
        }
    }

    /**
     * 권한 부여 설정 Activity 종료
     */
    fun verifyPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _notificationPermission.value = permissionUtil.verifyNotificationPermission().isEmpty()
                _activityPermission.value = permissionUtil.verifyActivityPermission().isEmpty()
                _locationPermission.value = permissionUtil.verifyLocationPermission().isEmpty()
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
package com.monglife.mongs.presentation.viewmodel.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.monglife.mongs.application.auth.usecase.GetIsLoginUseCase
import com.monglife.mongs.application.auth.usecase.GetMustUpdateAppUseCase
import com.monglife.mongs.core.presentation.utils.PermissionUtil
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LayoutViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
    private val getMustUpdateAppUseCase: GetMustUpdateAppUseCase,
    private val getIsLoginUseCase: GetIsLoginUseCase,
) : BaseViewModel() {

    val isLogin: LiveData<Boolean> get() = _isLogin
    private val _isLogin = MediatorLiveData(false)

    private val _requestPermissionEvent = MutableSharedFlow<Array<String>>()
    val requestPermissionEvent = _requestPermissionEvent.asSharedFlow()

    /**
     * 옵저빙 데이터 로드 (init)
     */
    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Loading

            // 로그인 여부 옵저빙 데이터 로드
            _isLogin.addSource(withContext(Dispatchers.IO) { getIsLoginUseCase() }.asLiveData()) {
                _isLogin.value = it
            }

            // 권한 확인
            val permissions =
                permissionUtil.verifyNotificationPermission() + permissionUtil.verifyActivityPermission() + permissionUtil.verifyLocationPermission()
            if (permissions.isNotEmpty()) {
                _requestPermissionEvent.emit(permissions.toTypedArray())
            } else {
                uiState = UiState.Idle
            }
        }
    }

    /**
     * 권한 부여 여부 확인
     */
    fun verifyPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
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
        val loadingBar: Boolean,
        val mustUpdateApp: Boolean,
    ) {
        data object Idle : UiState(loadingBar = false, mustUpdateApp = false)
        data object Loading : UiState(loadingBar = true, mustUpdateApp = false)
        data object NeedUpdate : UiState(loadingBar = false, mustUpdateApp = true)
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            // UI 초기화
            uiState = UiState.Loading

            runCatching {
                // 앱 버전 체크
                val mustUpdate = getMustUpdateAppUseCase()

                if (mustUpdate) {
                    uiState = UiState.NeedUpdate
                }
            }.onFailure {
                uiState = UiState.NeedUpdate
            }.onSuccess {
                uiState = UiState.Idle
            }
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        uiState = UiState.Loading
    }
}
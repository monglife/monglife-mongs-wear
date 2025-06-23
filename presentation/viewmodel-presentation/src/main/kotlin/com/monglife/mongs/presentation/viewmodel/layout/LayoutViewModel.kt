package com.monglife.mongs.presentation.viewmodel.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.monglife.mongs.application.auth.usecase.GetIsLoginUseCase
import com.monglife.mongs.application.auth.usecase.GetMustUpdateAppUseCase
import com.monglife.mongs.core.presentation.utils.PermissionUtil
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LayoutViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
    private val getMustUpdateAppUseCase: GetMustUpdateAppUseCase,
    private val getIsLoginUseCase: GetIsLoginUseCase,
) : BaseViewModel() {

    private val _isLogin = MediatorLiveData(false)
    val isLogin: LiveData<Boolean> get() = _isLogin

    private val _requestPermissionEvent = MutableSharedFlow<Array<String>>()
    val requestPermissionEvent = _requestPermissionEvent.asSharedFlow()

    init {

        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Loading

            // 앱 강제 업데이트 체크
            if (verifyAppVersion()) {
                uiState = UiState.NeedUpdate
                return@launch
            }

            // 권한 확인
            val permissions = buildList {
                addAll(permissionUtil.verifyNotificationPermission())
                addAll(permissionUtil.verifyActivityPermission())
                addAll(permissionUtil.verifyLocationPermission())
            }

            if (permissions.isNotEmpty()) {
                _requestPermissionEvent.emit(permissions.toTypedArray())
            }

            val isLoginFlow = getIsLoginUseCase().stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, false)
            _isLogin.value = isLoginFlow.first()


            uiState = UiState.Idle

            observeForever(isLoginFlow, _isLogin)
        }
    }

    /**
     * 앱 버전 체크
     */
    private suspend fun verifyAppVersion() =
        runCatching { getMustUpdateAppUseCase() }.getOrElse { true }

    /**
     * 권한 부여 여부 확인
     */
    fun verifyPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
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
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Idle
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        uiState = UiState.Loading
    }
}
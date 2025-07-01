package com.monglife.mongs.presentation.viewmodel.pages.main

import com.monglife.mongs.application.device.usecase.ObserveCurrentWalkingCountUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.presentation.utils.PermissionUtil
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainStepViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeCurrentWalkingCountUseCase: ObserveCurrentWalkingCountUseCase,
) : BaseViewModel() {

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
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _activityPermission = MutableStateFlow(false)
    val activityPermission: StateFlow<Boolean> = _activityPermission.asStateFlow()

    private val _mongVo = MutableStateFlow<MongVo?>(null)
    val mongVo: StateFlow<MongVo?> = _mongVo.asStateFlow()

    private val _currentWalkingCount = MutableStateFlow(0)
    val currentWalkingCount: StateFlow<Int> = _currentWalkingCount.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                // 활동 권한 정보 목록
                _activityPermission.value = permissionUtil.verifyActivityPermission().isEmpty()

                observeForever(observeCurrentMongUseCase(), _mongVo)
                observeForever(observeCurrentWalkingCountUseCase(), _currentWalkingCount)
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 활동 권한 체크
     */
    fun verifyActivityPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            _activityPermission.value = permissionUtil.verifyActivityPermission().isEmpty()
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
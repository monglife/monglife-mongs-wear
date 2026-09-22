package com.monglife.mongs.presentation.viewmodel.pages.main

import com.monglife.core.presentation.utils.PermissionUtil
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.device.usecase.ObserveCurrentWalkingCountUseCase
import com.monglife.mongs.application.device.usecase.StartStepCollectionUseCase
import com.monglife.mongs.application.device.vo.StepVo
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
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
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeCurrentWalkingCountUseCase: ObserveCurrentWalkingCountUseCase,
    private val startStepCollectionUseCase: StartStepCollectionUseCase,
    private val permissionUtil: PermissionUtil,
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

    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    // available = false 로 시작한다. 수집 경로가 정해지기 전에는 0 이 아니라 "-" 를 보여야 한다.
    private val _stepVo = MutableStateFlow(StepVo(walkingCount = 0, exchangeableWalkingCount = 0, available = false))
    val stepVo: StateFlow<StepVo> = _stepVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                // 활동 권한 정보 목록
                _activityPermission.value = permissionUtil.verifyActivityPermission().isEmpty()

                observeForever(observeCurrentMongUseCase(), _currentMongVo)
                observeForever(observeCurrentWalkingCountUseCase(), _stepVo)
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 활동 권한 체크
     */
    fun verifyActivityPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            val granted = permissionUtil.verifyActivityPermission().isEmpty()
            _activityPermission.value = granted

            // 권한이 없으면 수집 경로가 NONE 으로 내려가 있다. 방금 허용했다면 다시 해석해
            // 곧바로 걸음을 세기 시작하게 한다. 멱등이라 이미 수집 중이어도 무해하다.
            if (granted) startStepCollectionUseCase()
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
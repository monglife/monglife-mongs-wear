package com.monglife.mongs.presentation.viewmodel.pages.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.monglife.mongs.application.device.usecase.ObserveCurrentWalkingCountUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.presentation.utils.PermissionUtil
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainViewModel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainStepViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeCurrentWalkingCountUseCase: ObserveCurrentWalkingCountUseCase,
) : BaseViewModel() {

    private val _activityPermission = MediatorLiveData(true)
    val activityPermission: LiveData<Boolean> get() = _activityPermission

    private val _mongVo = MediatorLiveData<MongVo?>(null)
    val mongVo: LiveData<MongVo?> get() = _mongVo

    private val _currentWalkingCount = MediatorLiveData<Int>()
    val currentWalkingCount: LiveData<Int> get() = _currentWalkingCount

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Loading

            // 활동 권한 정보 목록
            _activityPermission.postValue(permissionUtil.verifyActivityPermission().isEmpty())

            uiState = UiState.Idle
        }

        // 몽 정보 옵저빙
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            observeCurrentMongUseCase().collect { _mongVo.postValue(it) }
        }

        // 현재 총 걸음 수 옵저빙
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            observeCurrentWalkingCountUseCase().collect { _currentWalkingCount.postValue(it) }
        }
    }

    /**
     * 활동 권한 체크
     */
    fun verifyActivityPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            _activityPermission.postValue(permissionUtil.verifyActivityPermission().isEmpty())
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
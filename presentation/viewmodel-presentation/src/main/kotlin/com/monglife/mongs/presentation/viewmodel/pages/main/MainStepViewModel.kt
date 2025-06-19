package com.monglife.mongs.presentation.viewmodel.pages.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.monglife.mongs.application.device.usecase.ObserveCurrentWalkingCountUseCase
import com.monglife.mongs.application.mong.usecase.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.presentation.utils.PermissionUtil
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainViewModel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainStepViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeCurrentWalkingCountUseCase: ObserveCurrentWalkingCountUseCase,
) : BaseViewModel() {

    private val _mongVo = MediatorLiveData<MongVo?>(null)
    val mongVo: LiveData<MongVo?> get() = _mongVo

    private val _currentWalkingCount = MediatorLiveData<Int>()
    val currentWalkingCount: LiveData<Int> get() = _currentWalkingCount

    private val _activityPermission = MediatorLiveData(true)
    val activityPermission: LiveData<Boolean> get() = _activityPermission

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Loading

            _mongVo.addSource(withContext(Dispatchers.IO) { observeCurrentMongUseCase().asLiveData() }) {
                _mongVo.value = it
            }

            _currentWalkingCount.addSource(withContext(Dispatchers.IO) { observeCurrentWalkingCountUseCase().asLiveData() }) {
                _currentWalkingCount.value = it
            }

            _activityPermission.postValue(permissionUtil.verifyActivityPermission().isEmpty())

            uiState = UiState.Idle
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
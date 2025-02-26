package com.mongs.wear.presentation.pages.main.walking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mongs.wear.core.exception.usecase.GetStepsUseCaseException
import com.mongs.wear.domain.device.usecase.GetStepsUseCase
import com.mongs.wear.domain.management.usecase.GetCurrentSlotUseCase
import com.mongs.wear.domain.management.vo.MongVo
import com.mongs.wear.presentation.global.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainWalkingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCurrentSlotUseCase: GetCurrentSlotUseCase,
    private val getStepsUseCase: GetStepsUseCase,
): BaseViewModel() {

    private val _mongVo = MediatorLiveData<MongVo?>()
    val mongVo: LiveData<MongVo?> get() = _mongVo

    private val _payPoint = MediatorLiveData<Int>()
    val payPoint: LiveData<Int> get() = _payPoint

    private val _steps = MediatorLiveData<Int>()
    val steps: LiveData<Int> get() = _steps

    val activityPermission: LiveData<Boolean> get() = _activityPermission
    private val _activityPermission = MediatorLiveData(false)

    init {
        viewModelScopeWithHandler.launch (Dispatchers.Main) {

            uiState.loadingBar = true

            _mongVo.addSource(withContext(Dispatchers.IO) { getCurrentSlotUseCase() }) { mongVo ->
                _mongVo.value = mongVo
            }

            _payPoint.addSource(withContext(Dispatchers.IO) { getCurrentSlotUseCase() }) {
                it?.let { mongVo ->
                    _payPoint.value = mongVo.payPoint
                } ?: run { _payPoint.value = 0 }
            }

            _steps.addSource(withContext(Dispatchers.IO) { getStepsUseCase() }) { steps ->
                _steps.value = steps
            }

            _activityPermission.postValue(verifyActivityPermission().isEmpty())

            uiState.loadingBar = false
        }
    }

    /**
     * 걸음 수 초기화
     */
    fun resetSteps() {
        viewModelScopeWithHandler.launch (Dispatchers.IO) {

            // TODO: 걸음 수 초기화

            uiState.resetStepsDialog = false
        }
    }

    /**
     * 활동 권한 부여 여부 갱신
     */
    fun refreshActivityPermission() {
        viewModelScopeWithHandler.launch (Dispatchers.IO) {
            _activityPermission.postValue(verifyActivityPermission().isEmpty())
        }
    }

    /**
     * 활동 권한 체크
     */
    private fun verifyActivityPermission() : Array<String> {
        return if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            emptyArray()
        }
    }

    val uiState: UiState = UiState()

    class UiState : BaseUiState() {
        var resetStepsDialog by mutableStateOf(false)
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        when(exception) {
            is GetStepsUseCaseException -> {
                uiState.loadingBar = false
            }

            else -> {
                uiState.loadingBar = false
            }
        }
    }
}
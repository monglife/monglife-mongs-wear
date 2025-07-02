package com.monglife.mongs.presentation.viewmodel.pages.training

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.monglife.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingRunnerViewModel @Inject constructor(

): BaseViewModel() {

    //TODO: observe data for view
//    private val _someData = MediatorLiveData<String>()
//    val someData: LiveData<String> get() = _someData
    // TODO: event for view
//    private val _someEvent = MutableSharedFlow<Int>()
//    val someEvent = _someEvent.asSharedFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Loading

            // TODO: load observe data
//            _someData.addSource(withContext(Dispatchers.IO) { observeUseCase().asLiveData() }) {
//                _someData.value = it
//            }

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
        val loadingBar: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        uiState = UiState.Idle
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        initialize()
    }
}
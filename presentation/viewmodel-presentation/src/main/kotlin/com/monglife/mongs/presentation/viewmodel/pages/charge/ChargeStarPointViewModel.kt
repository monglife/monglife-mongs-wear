package com.monglife.mongs.presentation.viewmodel.pages.charge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.monglife.mongs.application.member.store.vo.ProductVo
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChargeStarPointViewModel @Inject constructor(

): BaseViewModel() {

    //TODO: observe data for view
//    private val _someData = MediatorLiveData<String>()
//    val someData: LiveData<String> get() = _someData
    // TODO: event for view
//    private val _someEvent = MutableSharedFlow<Int>()
//    val someEvent = _someEvent.asSharedFlow()

    private val _starPoint = MediatorLiveData<Int>()
    val starPoint: LiveData<Int> get() = _starPoint

    private val _productVos = MediatorLiveData<List<ProductVo>>()
    val productVos: LiveData<List<ProductVo>> get() = _productVos

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
     * 인앱 상품 목록 조회
     */
    fun getProducts() {

    }

    /**
     * 인앱 상품 주문 및 소비
     */
    fun consumeWithOrder() {

    }

    /**
     * 인앱 상품 소비
     */
    fun consume() {

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
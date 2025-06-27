package com.monglife.core.common.skells/*

@HiltViewModel
class ViewModel @Inject constructor(

): BaseViewModel() {

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
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class SomeEvent(val param: String): UiEvent()
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * UI 이벤트 변수
     */
    private val _uiEvent = MutableStateFlow<UiEvent>(UiEvent.Idle)
    val uiEvent: StateFlow<UiEvent> = _uiEvent.asStateFlow()

    /**
     * 변수
     */
    private val _data = MutableStateFlow<Any>()
    val data: StateFlow<Any> get() = _data

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getDataFlow()
                    .stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, false)
                    .let {
                        observeForever(dataFlow, _data)
                        _data.value = it.first()
                    }
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

*/
package com.monglife.mongs.presentation.viewmodel.pages.inventory

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.usecase.interaction.GetInventoriesUseCase
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.InventoryVo
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.presentation.viewmodel.pages.feed.FeedFoodViewModel.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val getInventoriesUseCase: GetInventoriesUseCase,
): BaseViewModel() {

    companion object {
        private const val INIT_PAGE = 1
        private const val INIT_SIZE = 4
    }

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
        data class NavMenu(val message: String): UiEvent()
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * UI 이벤트 변수
     */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    /**
     * 변수
     */
    private val _page = MutableStateFlow(INIT_PAGE)
    val page: StateFlow<Int> = _page.asStateFlow()

    private val _size = MutableStateFlow(INIT_SIZE)
    val size: StateFlow<Int> = _size.asStateFlow()

    private val _totalPage = MutableStateFlow(0)
    val totalPage: StateFlow<Int> = _totalPage.asStateFlow()

    private val _isLastPage = MutableStateFlow(true)
    val isLastPage: StateFlow<Boolean> = _isLastPage.asStateFlow()

    private val _inventoryVos = MutableStateFlow<List<InventoryVo>>(emptyList())
    val inventoryVos: StateFlow<List<InventoryVo>> = _inventoryVos.asStateFlow()

    private val _mongVo = MutableStateFlow<MongVo?>(null)

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getCurrentMongUseCase()?.let {
                    _mongVo.value = it
                } ?: run {
                    _uiEvent.emit(UiEvent.NavMenu("선택된 몽이 없음"))
                    return@withContext
                }

                updateInventoryVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 페이지 변경
     */
    fun changePage(page: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading
            _page.value = page

            withContext(Dispatchers.IO) { updateInventoryVos() }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 인벤토리 목록 조회
     */
    private suspend fun updateInventoryVos() {
        _mongVo.value?.let {
            getInventoriesUseCase(
                command = GetInventoriesUseCase.Command(
                    mongId = it.mongId,
                    page = _page.value,
                    size = INIT_SIZE,
                )
            ).let { inventoryVosPage ->
                _page.value = inventoryVosPage.page
                _size.value = inventoryVosPage.size
                _totalPage.value = inventoryVosPage.totalPage
                _isLastPage.value = inventoryVosPage.isLastPage
                _inventoryVos.value = inventoryVosPage.result
            }
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
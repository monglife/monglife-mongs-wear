package com.monglife.mongs.presentation.viewmodel.pages.inventory

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.usecase.interaction.ConsumeInventoryUseCase
import com.monglife.mongs.application.mong.usecase.interaction.GetInventoriesUseCase
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.InventoryVo
import com.monglife.mongs.application.mong.vo.MongVo
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
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val getInventoriesUseCase: GetInventoriesUseCase,
    private val consumeInventoryUseCase: ConsumeInventoryUseCase,
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
        val confirmDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Confirm : UiState(confirmDialogOpen = true)
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
    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

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

    private val _currentInventoryVo = MutableStateFlow<InventoryVo?>(null)
    val currentInventoryVo: StateFlow<InventoryVo?> = _currentInventoryVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getCurrentMongUseCase()?.let {
                    _currentMongVo.value = it
                } ?: run {
                    _uiEvent.emit(UiEvent.NavMenu("선택된 몽이 없음"))
                    return@withContext
                }

                observeForever(observeCurrentMongUseCase(), _currentMongVo)

                this@InventoryViewModel.updateInventoryVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 이전 페이지
     */
    fun prevPage() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading
            _page.value = max(_page.value - 1, INIT_PAGE)

            withContext(Dispatchers.IO) {
                this@InventoryViewModel.updateInventoryVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 다음 페이지
     */
    fun nextPage() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading
            _page.value = min(_page.value + 1, _totalPage.value)

            withContext(Dispatchers.IO) {
                this@InventoryViewModel.updateInventoryVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 인벤토리 소비
     */
    fun consumeInventory(mongId: Long, inventoryId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading
            _page.value = INIT_PAGE

            withContext(Dispatchers.IO) {
                consumeInventoryUseCase(
                    command = ConsumeInventoryUseCase.Command(
                        inventoryId = inventoryId,
                        mongId = mongId,
                    )
                )

                this@InventoryViewModel.updateInventoryVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 인벤토리 소비 확인 다이얼로그 오픈
     */
    fun consumeInventoryConfirmDialogOpen(inventoryVo: InventoryVo) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _currentInventoryVo.value = inventoryVo
            _uiState.value = UiState.Confirm
        }
    }

    /**
     * 인벤토리 소비 확인 다이얼로그 닫기
     */
    fun consumeInventoryConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
            _currentInventoryVo.value = null
        }
    }

    /**
     * 인벤토리 목록 조회
     */
    private suspend fun updateInventoryVos() {
        _currentMongVo.value?.let {
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
package com.monglife.mongs.presentation.viewmodel.pages.inventory

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.exception.InvalidConsumeInventoryException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.usecase.interaction.ConsumeInventoryUseCase
import com.monglife.mongs.application.mong.usecase.interaction.GetFoodsUseCase
import com.monglife.mongs.application.mong.usecase.interaction.GetInventoriesUseCase
import com.monglife.mongs.application.mong.usecase.interaction.GetSnacksUseCase
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
    private val getFoodsUseCase: GetFoodsUseCase,
    private val getSnacksUseCase: GetSnacksUseCase,
    private val consumeInventoryUseCase: ConsumeInventoryUseCase,
): BaseViewModel() {

    companion object {
        /**
         * 밥/간식 화면처럼 한 칸씩 넘겨 보므로, 화살표를 누를 때마다 서버를 부르지 않도록
         * 목록을 처음에 전부 받아 둔다.
         *
         * 서버가 size 를 10 이하로 제한하므로(GLOBAL-ERROR-000) 이 값이 상한이고,
         * 10 개를 넘는 인벤토리는 getAllInventoryVos 가 다음 페이지를 이어 받는다.
         */
        private const val PAGE_SIZE = 10
    }

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val detailDialogOpen: Boolean = false,
        val confirmDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Detail : UiState(detailDialogOpen = true)
        data object Confirm : UiState(confirmDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class NavMain(val message: String): UiEvent()
        data object Consume: UiEvent()
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

    private val _inventoryVos = MutableStateFlow<List<InventoryVo>>(emptyList())
    val inventoryVos: StateFlow<List<InventoryVo>> = _inventoryVos.asStateFlow()

    private val _currentInventoryVo = MutableStateFlow<InventoryVo?>(null)
    val currentInventoryVo: StateFlow<InventoryVo?> = _currentInventoryVo.asStateFlow()

    private val _inventoryVoIndex = MutableStateFlow(0)
    val inventoryVoIndex: StateFlow<Int> = _inventoryVoIndex.asStateFlow()

    /**
     * 인벤토리 응답에는 스텟 증가량이 없다. 밥/간식 목록에서 코드로 찾아 붙여
     * 밥/간식 화면과 같은 상세 다이얼로그를 띄운다. 코드가 목록에 없으면 null 이고,
     * 그 경우 아이콘을 눌러도 아무 일도 일어나지 않는다.
     */
    private val _inventoryStatusVos = MutableStateFlow<Map<String, InventoryStatusVo>>(emptyMap())

    private val _currentInventoryStatusVo = MutableStateFlow<InventoryStatusVo?>(null)
    val currentInventoryStatusVo: StateFlow<InventoryStatusVo?> = _currentInventoryStatusVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getCurrentMongUseCase()?.let {
                    _currentMongVo.value = it

                    _inventoryStatusVos.value = this@InventoryViewModel.getInventoryStatusVos(mongId = it.mongId)
                    _inventoryVos.value = this@InventoryViewModel.getAllInventoryVos(mongId = it.mongId)

                    this@InventoryViewModel.syncCurrentInventoryVo()
                } ?: run {
                    _uiEvent.emit(UiEvent.NavMain("선택된 몽이 없음"))
                    return@withContext
                }

                observeForever(observeCurrentMongUseCase(), _currentMongVo)
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 이전 아이템
     */
    fun prevInventory() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _inventoryVoIndex.value = max(_inventoryVoIndex.value - 1, 0)

            this@InventoryViewModel.syncCurrentInventoryVo()
        }
    }

    /**
     * 다음 아이템
     */
    fun nextInventory() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _inventoryVoIndex.value = min(_inventoryVoIndex.value + 1, _inventoryVos.value.size - 1)

            this@InventoryViewModel.syncCurrentInventoryVo()
        }
    }

    /**
     * 인벤토리 소비
     */
    fun consumeInventory(mongId: Long, inventoryId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                consumeInventoryUseCase(
                    command = ConsumeInventoryUseCase.Command(
                        inventoryId = inventoryId,
                        mongId = mongId,
                    )
                )

                _uiEvent.emit(UiEvent.Consume)
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 상세 다이얼로그 오픈
     */
    fun detailDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Detail
        }
    }

    /**
     * 상세 다이얼로그 닫기
     */
    fun detailDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 인벤토리 소비 확인 다이얼로그 오픈
     */
    fun consumeConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Confirm
        }
    }

    /**
     * 인벤토리 소비 확인 다이얼로그 닫기
     */
    fun consumeConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 인벤토리 목록 전체 조회
     */
    private suspend fun getAllInventoryVos(mongId: Long): List<InventoryVo> {
        val inventoryVos = mutableListOf<InventoryVo>()

        var page = 1
        var totalPage = 1

        do {
            getInventoriesUseCase(
                command = GetInventoriesUseCase.Command(
                    mongId = mongId,
                    page = page,
                    size = PAGE_SIZE,
                )
            ).let { inventoryVoPage ->
                inventoryVos.addAll(inventoryVoPage.result)
                totalPage = inventoryVoPage.totalPage
            }

            page++
        } while (page <= totalPage)

        return inventoryVos
    }

    /**
     * 밥/간식 목록으로 아이템 코드별 스텟 증가량 조회
     */
    private suspend fun getInventoryStatusVos(mongId: Long): Map<String, InventoryStatusVo> = buildMap {
        getFoodsUseCase(command = GetFoodsUseCase.Command(mongId = mongId)).forEach {
            put(
                it.foodCode,
                InventoryStatusVo(
                    weight = it.weight,
                    strength = it.strength,
                    satiety = it.satiety,
                    healthy = it.healthy,
                    fatigue = it.fatigue,
                )
            )
        }

        getSnacksUseCase(command = GetSnacksUseCase.Command(mongId = mongId)).forEach {
            put(
                it.snackCode,
                InventoryStatusVo(
                    weight = it.weight,
                    strength = it.strength,
                    satiety = it.satiety,
                    healthy = it.healthy,
                    fatigue = it.fatigue,
                )
            )
        }
    }

    /**
     * 현재 인덱스의 아이템과 그 스텟을 맞춘다
     */
    private fun syncCurrentInventoryVo() {
        val inventoryVo = _inventoryVos.value.getOrNull(_inventoryVoIndex.value)

        _currentInventoryVo.value = inventoryVo
        _currentInventoryStatusVo.value = inventoryVo?.let { _inventoryStatusVos.value[it.inventoryCode] }
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
        when (exception) {
            is NotFoundMongException -> _uiEvent.emit(UiEvent.NavMain("잠시후 다시 시도"))
            is InvalidConsumeInventoryException -> _uiState.value = UiState.Idle
            else -> initialize()
        }
    }

    /**
     * 인벤토리 아이템 스텟 증가량 Vo
     */
    data class InventoryStatusVo(
        val weight: Double,
        val strength: Double,
        val satiety: Double,
        val healthy: Double,
        val fatigue: Double,
    )
}

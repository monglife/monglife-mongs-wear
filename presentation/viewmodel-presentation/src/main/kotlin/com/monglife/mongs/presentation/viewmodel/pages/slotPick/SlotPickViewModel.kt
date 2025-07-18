package com.monglife.mongs.presentation.viewmodel.pages.slotPick

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.member.player.usecase.BuySlotUseCase
import com.monglife.mongs.application.member.player.usecase.ObservePlayerUseCase
import com.monglife.mongs.application.mong.usecase.management.CreateMongUseCase
import com.monglife.mongs.application.mong.usecase.management.DeleteMongUseCase
import com.monglife.mongs.application.mong.usecase.management.GetMongsUseCase
import com.monglife.mongs.application.mong.usecase.management.GraduateMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.SetCurrentMongIdUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.presentation.viewmodel.pages.slotPick.vo.SlotVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class SlotPickViewModel @Inject constructor(
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observePlayerUseCase: ObservePlayerUseCase,
    private val getMongsUseCase: GetMongsUseCase,
    private val createMongUseCase: CreateMongUseCase,
    private val deleteMongUseCase: DeleteMongUseCase,
    private val setCurrentMongIdUseCase: SetCurrentMongIdUseCase,
    private val graduateMongUseCase: GraduateMongUseCase,
    private val buySlotUseCase: BuySlotUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val createDialogOpen: Boolean = false,
        val detailDialogOpen: Boolean = false,
        val deleteConfirmDialogOpen: Boolean = false,
        val pickConfirmDialogOpen: Boolean = false,
        val graduateConfirmDialogOpen: Boolean = false,
        val buySlotConfirmDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Create : UiState(createDialogOpen = true)
        data object Detail : UiState(detailDialogOpen = true)
        data object DeleteConfirm : UiState(deleteConfirmDialogOpen = true)
        data object PickConfirm : UiState(pickConfirmDialogOpen = true)
        data object GraduateConfirm: UiState(graduateConfirmDialogOpen = true)
        data object BuySlotConfirm: UiState(buySlotConfirmDialogOpen = true)
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    private val _slotCount = MutableStateFlow(0)

    private val _starPoint = MutableStateFlow(0)
    val starPoint: StateFlow<Int> = _starPoint.asStateFlow()

    private val _slotVos = MutableStateFlow<List<SlotVo>>(emptyList())
    val slotVos: StateFlow<List<SlotVo>> = _slotVos.asStateFlow()

    private val _slotVoIndex = MutableStateFlow(0)
    val slotVoIndex: StateFlow<Int> = _slotVoIndex.asStateFlow()

    private val _currentSlotVo = MutableStateFlow<SlotVo?>(null)
    val currentSlotVo: StateFlow<SlotVo?> = _currentSlotVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                observeForever(observeCurrentMongUseCase(), _currentMongVo)

                observePlayerUseCase()
                    .let { flow ->
                        observeForever(flow.map { it.slotCount }, _slotCount)
                        observeForever(flow.map { it.starPoint }, _starPoint)
                    }

                // 몽 목록 정보 조회
                this@SlotPickViewModel.updateSlotVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    fun nextSlot() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _slotVoIndex.value = min(_slotVoIndex.value + 1, _slotVos.value.size - 1)
            this@SlotPickViewModel.updateCurrentSlotVo()
        }
    }

    fun prevSlot() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _slotVoIndex.value = max(_slotVoIndex.value - 1, 0)
            this@SlotPickViewModel.updateCurrentSlotVo()
        }
    }

    /**
     * 몽 생성
     */
    fun createMong(name: String, sleepAt: String, wakeupAt: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                createMongUseCase(
                    command = CreateMongUseCase.Command(
                        name = name,
                        sleepAt = LocalTime.parse(sleepAt),
                        wakeupAt = LocalTime.parse(wakeupAt),
                    )
                )

                // 몽 목록 정보 조회
                this@SlotPickViewModel.updateSlotVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 몽 삭제
     */
    fun deleteMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                deleteMongUseCase(
                    command = DeleteMongUseCase.Command(
                        mongId = mongId,
                    )
                )

                // 몽 목록 정보 조회
                this@SlotPickViewModel.updateSlotVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 몽 선택
     */
    fun pickMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                setCurrentMongIdUseCase(
                    command = SetCurrentMongIdUseCase.Command(
                        mongId = mongId,
                    )
                )
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 몽 졸업
     */
    fun graduateMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                graduateMongUseCase(
                    command = GraduateMongUseCase.Command(
                        mongId = mongId,
                    )
                )

                // 몽 목록 정보 조회
                this@SlotPickViewModel.updateSlotVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 슬롯 구매
     */
    fun buySlot() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                buySlotUseCase()
                this@SlotPickViewModel.updateSlotVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 몽 생성 다이얼로그 열기
     */
    fun createDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Create
        }
    }

    /**
     * 몽 상세 정보 다이얼로그 열기
     */
    fun detailDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Detail
        }
    }

    /**
     * 몽 삭제 다이얼로그 열기
     */
    fun deleteConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.DeleteConfirm
        }
    }

    /**
     * 몽 선택 다이얼로그 열기
     */
    fun pickConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.PickConfirm
        }
    }

    /**
     * 몽 졸업 다이얼로그 열기
     */
    fun graduateConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.GraduateConfirm
        }
    }

    /**
     * 슬롯 구매 다이얼로그 열기
     */
    fun buySlotConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.BuySlotConfirm
        }
    }

    private suspend fun updateSlotVos() {
        // 몽 목록 정보 조회
        _slotVos.value = getMongsUseCase().let {
            val slotVos = it.map { mongVo ->
                SlotVo(
                    type = SlotVo.SlotType.EXISTS,
                    mongVo = mongVo
                )
            } as ArrayList

            repeat((_slotCount.value - it.size).coerceAtLeast(0)) {
                slotVos.add(SlotVo(type = SlotVo.SlotType.EMPTY))
            }

            if (slotVos.size < 3) {
                slotVos.add(SlotVo(type = SlotVo.SlotType.BUY))
            }

            slotVos
        }

        this@SlotPickViewModel.updateCurrentSlotVo()
    }

    private fun updateCurrentSlotVo() {
        if (_slotVoIndex.value in 0..<_slotVos.value.size) {
            _currentSlotVo.value = _slotVos.value[_slotVoIndex.value]
        } else {
            _currentSlotVo.value = null
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
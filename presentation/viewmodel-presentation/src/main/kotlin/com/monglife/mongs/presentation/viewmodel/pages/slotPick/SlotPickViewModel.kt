package com.monglife.mongs.presentation.viewmodel.pages.slotPick

import com.monglife.mongs.application.member.player.usecase.BuySlotUseCase
import com.monglife.mongs.application.member.player.usecase.ObservePlayerUseCase
import com.monglife.mongs.application.mong.usecase.management.CreateMongUseCase
import com.monglife.mongs.application.mong.usecase.management.DeleteMongUseCase
import com.monglife.mongs.application.mong.usecase.management.GetMongsUseCase
import com.monglife.mongs.application.mong.usecase.management.GraduateMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.SetCurrentMongIdUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SlotPickViewModel @Inject constructor(
    private val getMongsUseCase: GetMongsUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observePlayerUseCase: ObservePlayerUseCase,
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
        val deleteDialogOpen: Boolean = false,
        val pickDialogOpen: Boolean = false,
        val graduateDialogOpen: Boolean = false,
        val buySlotDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Create : UiState(createDialogOpen = true)
        data object Detail : UiState(detailDialogOpen = true)
        data object Delete : UiState(deleteDialogOpen = true)
        data object Pick : UiState(pickDialogOpen = true)
        data object Graduate: UiState(graduateDialogOpen = true)
        data object BuySlot: UiState(buySlotDialogOpen = true)
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _mongVo = MutableStateFlow<MongVo?>(null)
    val mongVo: StateFlow<MongVo?> = _mongVo.asStateFlow()

    private val _mongVos = MutableStateFlow<List<MongVo>>(emptyList())
    val mongVos: StateFlow<List<MongVo>> get() = _mongVos

    private val _slotCount = MutableStateFlow(0)
    val slotCount: StateFlow<Int> get() = _slotCount

    private val _starPoint = MutableStateFlow(0)
    val starPoint: StateFlow<Int> get() = _starPoint

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _mongVos.value = getMongsUseCase()

                observeCurrentMongUseCase()
                    .stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, null)
                    .let {
                        observeForever(it, _mongVo)
                        _mongVo.value = it.first()
                    }

                observePlayerUseCase()
                    .let { playerVoFlow ->
                        playerVoFlow.map { it.slotCount }
                            .stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, 0)
                            .let {
                                observeForever(it, _slotCount)
                                _slotCount.value = it.first()
                            }
                        playerVoFlow.map { it.starPoint }
                            .stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, 0)
                            .let {
                                observeForever(it, _starPoint)
                                _starPoint.value = it.first()
                            }
                    }
            }

            _uiState.value = UiState.Idle
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
                _mongVos.value = getMongsUseCase()
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
                _mongVos.value = getMongsUseCase()
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

                // 몽 목록 정보 조회
                _mongVos.value = getMongsUseCase()
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
                _mongVos.value = getMongsUseCase()
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
    fun deleteDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Delete
        }
    }

    /**
     * 몽 선택 다이얼로그 열기
     */
    fun pickDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Pick
        }
    }

    /**
     * 몽 졸업 다이얼로그 열기
     */
    fun graduateDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Graduate
        }
    }

    /**
     * 슬롯 구매 다이얼로그 열기
     */
    fun buySlotDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.BuySlot
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

    /**
     * 슬롯 Vo
     */
    data class SlotVo(
        val type: SlotType,
        val mongVo: MongVo? = null,
    ) {
        enum class SlotType {
            EXISTS,
            EMPTY,
            BUY,
        }
    }
}
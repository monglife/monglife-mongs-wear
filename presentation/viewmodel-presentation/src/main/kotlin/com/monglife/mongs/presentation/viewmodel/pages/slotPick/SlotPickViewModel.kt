package com.monglife.mongs.presentation.viewmodel.pages.slotPick

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.monglife.mongs.application.member.player.usecase.BuySlotUseCase
import com.monglife.mongs.application.member.player.usecase.ObserveSlotCountUseCase
import com.monglife.mongs.application.member.player.usecase.ObserveStarPointUseCase
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SlotPickViewModel @Inject constructor(
    private val getMongsUseCase: GetMongsUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val observeSlotCountUseCase: ObserveSlotCountUseCase,
    private val observeStarPointUseCase: ObserveStarPointUseCase,
    private val createMongUseCase: CreateMongUseCase,
    private val deleteMongUseCase: DeleteMongUseCase,
    private val setCurrentMongIdUseCase: SetCurrentMongIdUseCase,
    private val graduateMongUseCase: GraduateMongUseCase,
    private val buySlotUseCase: BuySlotUseCase,
): BaseViewModel() {

    private val _mongVo = MediatorLiveData<MongVo?>(null)
    val mongVo: LiveData<MongVo?> get() = _mongVo

    private val _mongVos = MediatorLiveData<List<MongVo>>()
    val mongVos: LiveData<List<MongVo>> get() = _mongVos

    private val _slotCount = MediatorLiveData<Int>()
    val slotCount: LiveData<Int> get() = _slotCount

    private val _starPoint = MediatorLiveData<Int>()
    val starPoint: LiveData<Int> get() = _starPoint

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Loading

            val mongVosDeferred = async { getMongsUseCase() }
            val currentMongFlow = observeCurrentMongUseCase().stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, null)
            val slotCountFlow = observeSlotCountUseCase().stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, 0)
            val starPointFlow = observeStarPointUseCase().stateIn(viewModelScopeWithHandler, SharingStarted.Eagerly, 0)

            _mongVos.postValue(mongVosDeferred.await())
            _mongVo.postValue(withContext(Dispatchers.IO) { currentMongFlow.first { it != null } })
            _slotCount.postValue(withContext(Dispatchers.IO) { slotCountFlow.first() })
            _starPoint.postValue(withContext(Dispatchers.IO) { starPointFlow.first() })

            uiState = UiState.Idle

            // 옵저빙 객체 선언
            observeForever(currentMongFlow, _mongVo)
            observeForever(slotCountFlow, _slotCount)
            observeForever(starPointFlow, _starPoint)
        }
    }

    /**
     * 몽 생성
     */
    fun createMong(name: String, sleepAt: String, wakeupAt: String) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Loading

            createMongUseCase(
                command = CreateMongUseCase.Command(
                    name = name,
                    sleepAt = LocalTime.parse(sleepAt),
                    wakeupAt = LocalTime.parse(wakeupAt),
                )
            )

            // 몽 목록 정보 조회
            _mongVos.postValue(getMongsUseCase())

            uiState = UiState.Idle
        }
    }

    /**
     * 몽 삭제
     */
    fun deleteMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Loading

            deleteMongUseCase(
                command = DeleteMongUseCase.Command(
                    mongId = mongId,
                )
            )

            // 몽 목록 정보 조회
            _mongVos.postValue(getMongsUseCase())

            uiState = UiState.Idle
        }
    }

    /**
     * 몽 선택
     */
    fun pickMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Loading

            setCurrentMongIdUseCase(
                command = SetCurrentMongIdUseCase.Command(
                    mongId = mongId,
                )
            )

            // 몽 목록 정보 조회
            _mongVos.postValue(getMongsUseCase())

            uiState = UiState.Idle
        }
    }

    /**
     * 몽 졸업
     */
    fun graduateMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Loading

            graduateMongUseCase(
                command = GraduateMongUseCase.Command(
                    mongId = mongId,
                )
            )

            // 몽 목록 정보 조회
            _mongVos.postValue(getMongsUseCase())

            uiState = UiState.Idle
        }
    }

    /**
     * 슬롯 구매
     */
    fun buySlot() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Loading

            buySlotUseCase()

            uiState = UiState.Idle
        }
    }

    /**
     * 몽 생성 다이얼로그 열기
     */
    fun createDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Create
        }
    }

    /**
     * 몽 상세 정보 다이얼로그 열기
     */
    fun detailDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Detail
        }
    }

    /**
     * 몽 삭제 다이얼로그 열기
     */
    fun deleteDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Delete
        }
    }

    /**
     * 몽 선택 다이얼로그 열기
     */
    fun pickDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Pick
        }
    }

    /**
     * 몽 졸업 다이얼로그 열기
     */
    fun graduateDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Graduate
        }
    }

    /**
     * 슬롯 구매 다이얼로그 열기
     */
    fun buySlotDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.BuySlot
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
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Idle
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
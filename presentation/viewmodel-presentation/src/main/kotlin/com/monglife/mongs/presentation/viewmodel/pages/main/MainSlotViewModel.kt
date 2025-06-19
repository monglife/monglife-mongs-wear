package com.monglife.mongs.presentation.viewmodel.pages.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.monglife.mongs.application.device.usecase.GetInitNotificationDialogOpenOptionUseCase
import com.monglife.mongs.application.device.usecase.SetInitNotificationDialogOpenOptionUseCase
import com.monglife.mongs.application.mong.usecase.EvolutionMongUseCase
import com.monglife.mongs.application.mong.usecase.GraduateCheckingUseCase
import com.monglife.mongs.application.mong.usecase.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.PoopCleanMongUseCase
import com.monglife.mongs.application.mong.usecase.SleepingMongUseCase
import com.monglife.mongs.application.mong.usecase.StrokeMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainSlotViewModel @Inject constructor(
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val getInitNotificationDialogOpenOptionUseCase: GetInitNotificationDialogOpenOptionUseCase,
    private val setInitNotificationDialogOpenOptionUseCase: SetInitNotificationDialogOpenOptionUseCase,
    private val strokeMongUseCase: StrokeMongUseCase,
    private val evolutionMongUseCase: EvolutionMongUseCase,
    private val graduateCheckingUseCase: GraduateCheckingUseCase,
    private val sleepingMongUseCase: SleepingMongUseCase,
    private val poopCleanMongUseCase: PoopCleanMongUseCase,
) : BaseViewModel() {

    private val _mongVo = MediatorLiveData<MongVo?>(null)
    val mongVo: LiveData<MongVo?> get() = _mongVo

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            uiState = UiState.Loading

            _mongVo.addSource(withContext(Dispatchers.IO) { observeCurrentMongUseCase().asLiveData() }) {
                _mongVo.value = it
            }

            uiState = if (getInitNotificationDialogOpenOptionUseCase()) UiState.InitNotification else UiState.Idle
        }
    }

    /**
     * 초기 다이얼로그 닫기
     */
    fun initDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Idle
        }
    }

    /**
     * 초기 다이얼로그 다시 보지 않기 옵션 설정 및 닫기
     */
    fun initDialogCloseForever() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            setInitNotificationDialogOpenOptionUseCase(
                command = SetInitNotificationDialogOpenOptionUseCase.Command(
                    isOpen = false
                )
            )
            uiState = UiState.Idle
        }
    }

    /**
     * 상호 작용 다이얼로그 오픈
     */
    fun interactionDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Interaction
        }
    }

    /**
     * 상호 작용 다이얼로그 닫기
     */
    fun interactionDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Idle
        }
    }

    /**
     * 쓰다듬기
     */
    fun strokeMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            if (uiState != UiState.Happy) {
                strokeMongUseCase(
                    command = StrokeMongUseCase.Command(
                        mongId = mongId,
                    )
                )

                uiState = UiState.Happy
                delay(3000)
                uiState = UiState.Idle
            }
        }
    }

    /**
     * 수면, 기상
     */
    fun sleepMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            sleepingMongUseCase(
                command = SleepingMongUseCase.Command(
                    mongId = mongId
                )
            )
        }
    }

    /**
     * 배변 처리
     */
    fun poopCleanMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            if (uiState != UiState.PoopClean) {
                poopCleanMongUseCase(
                    command = PoopCleanMongUseCase.Command(
                        mongId = mongId
                    )
                )

                uiState = UiState.PoopClean
                delay(3000)
                uiState = UiState.Idle
            }
        }
    }

    /**
     * 진화 이펙트 시작
     */
    fun evolutionMong() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Evolution
        }
    }

    /**
     * 진화
     */
    fun evolutionMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            if (uiState == UiState.Evolution) {
                evolutionMongUseCase(
                    command = EvolutionMongUseCase.Command(
                        mongId = mongId,
                    )
                )
                uiState = UiState.Idle
            }
        }
    }

    /**
     * 졸업 준비 여부 사용자 확인
     */
    fun graduateMongCheck(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            graduateCheckingUseCase(
                command = GraduateCheckingUseCase.Command(
                    mongId = mongId
                )
            )
        }
    }

    /**
     * 섭취 이벤트 발생
     * for FeedNested Route
     */
    fun eatingEvent() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            uiState = UiState.Eating
            delay(3000)
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
        val initNotificationDialogOpen: Boolean = false,
        val interactionDialogOpen: Boolean = false,
        val isHappy: Boolean = false,
        val isEating: Boolean = false,
        val isPoopCleaning: Boolean = false,
        val isEvolving: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object InitNotification : UiState(initNotificationDialogOpen = true)
        data object Interaction : UiState(interactionDialogOpen = true)
        data object Happy : UiState(isHappy = true)
        data object Eating : UiState(isEating = true)
        data object PoopClean : UiState(isPoopCleaning = true)
        data object Evolution : UiState(isEvolving = true)
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        // UI 초기화
        if (uiState != UiState.InitNotification) {
            uiState = UiState.Idle
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        initialize()
    }
}
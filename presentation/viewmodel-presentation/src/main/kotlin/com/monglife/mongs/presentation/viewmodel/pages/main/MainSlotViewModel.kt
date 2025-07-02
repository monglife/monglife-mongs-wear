package com.monglife.mongs.presentation.viewmodel.pages.main

import com.monglife.mongs.application.device.usecase.GetInitNotificationDialogOpenOptionUseCase
import com.monglife.mongs.application.device.usecase.SetInitNotificationDialogOpenOptionUseCase
import com.monglife.mongs.application.mong.usecase.management.EvolutionMongUseCase
import com.monglife.mongs.application.mong.usecase.management.GraduateCheckingUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.PoopCleanMongUseCase
import com.monglife.mongs.application.mong.usecase.management.SleepingMongUseCase
import com.monglife.mongs.application.mong.usecase.management.StrokeMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
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

    companion object {
        private const val EFFECT_DELAY = 4000L
    }

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val effectLoadingBar: Boolean = false,
        val initNotificationDialogOpen: Boolean = false,
        val interactionDialogOpen: Boolean = false,
        val isHappy: Boolean = false,
        val isEating: Boolean = false,
        val isPoopCleaning: Boolean = false,
        val isEvolving: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object EffectLoading : UiState(effectLoadingBar = true)
        data object InitNotification : UiState(initNotificationDialogOpen = true)
        data object Interaction : UiState(interactionDialogOpen = true)
        data object Happy : UiState(isHappy = true)
        data object Eating : UiState(isEating = true)
        data object PoopClean : UiState(isPoopCleaning = true)
        data object Evolution : UiState(isEvolving = true)
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _mongVo = MutableStateFlow<MongVo?>(null)
    val mongVo: StateFlow<MongVo?> = _mongVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            val initNotificationDialogOpen = withContext(Dispatchers.IO) {
                observeCurrentMongUseCase()
                    .shareIn(viewModelScopeWithHandler, SharingStarted.Eagerly, replay = 1)
                    .let { flow -> observeForever(flow, _mongVo) }
                getInitNotificationDialogOpenOptionUseCase()
            }

            _uiState.value = if (initNotificationDialogOpen) UiState.InitNotification else UiState.Idle
        }
    }

    /**
     * 초기 다이얼로그 닫기
     */
    fun initDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 초기 다이얼로그 다시 보지 않기 옵션 설정 및 닫기
     */
    fun initDialogCloseForever() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                setInitNotificationDialogOpenOptionUseCase(
                    command = SetInitNotificationDialogOpenOptionUseCase.Command(
                        isOpen = false
                    )
                )
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 상호 작용 다이얼로그 오픈
     */
    fun interactionDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Interaction
        }
    }

    /**
     * 상호 작용 다이얼로그 닫기
     */
    fun interactionDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 쓰다듬기
     */
    fun strokeMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            if (_uiState.value != UiState.Happy) {
                _uiState.value = UiState.EffectLoading

                withContext(Dispatchers.IO) {
                    strokeMongUseCase(
                        command = StrokeMongUseCase.Command(
                            mongId = mongId,
                        )
                    )
                }

                _uiState.value = UiState.Happy
                delay(EFFECT_DELAY)
                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 수면, 기상
     */
    fun sleepMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.EffectLoading

            withContext(Dispatchers.IO) {
                sleepingMongUseCase(
                    command = SleepingMongUseCase.Command(
                        mongId = mongId
                    )
                )
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 배변 처리
     */
    fun poopCleanMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            if (_uiState.value != UiState.PoopClean) {
                _uiState.value = UiState.EffectLoading

                withContext(Dispatchers.IO) {
                    poopCleanMongUseCase(
                        command = PoopCleanMongUseCase.Command(
                            mongId = mongId
                        )
                    )
                }

                _uiState.value = UiState.PoopClean
                delay(EFFECT_DELAY)
                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 진화 이펙트 시작
     */
    fun evolutionMong() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Evolution
        }
    }

    /**
     * 진화
     */
    fun evolutionMong(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            if (_uiState.value == UiState.Evolution) {
                withContext(Dispatchers.IO) {
                    evolutionMongUseCase(
                        command = EvolutionMongUseCase.Command(
                            mongId = mongId,
                        )
                    )
                }

                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 졸업 준비 여부 사용자 확인
     */
    fun graduateMongCheck(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                graduateCheckingUseCase(
                    command = GraduateCheckingUseCase.Command(
                        mongId = mongId
                    )
                )
            }
        }
    }

    /**
     * 섭취 이벤트 발생
     * for FeedNested Route
     */
    fun eatingEvent() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Eating
            delay(EFFECT_DELAY)
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            if (_uiState.value != UiState.InitNotification) {
                _uiState.value = UiState.Idle
            }
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        initialize()
    }
}
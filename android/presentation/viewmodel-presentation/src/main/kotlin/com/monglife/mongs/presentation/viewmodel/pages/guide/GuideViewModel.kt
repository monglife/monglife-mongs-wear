package com.monglife.mongs.presentation.viewmodel.pages.guide

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.device.usecase.GetInitGuideOpenOptionUseCase
import com.monglife.mongs.application.device.usecase.SetInitGuideOpenOptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.min

/**
 * 최초 진입 가이드 ViewModel.
 *
 * 가이드 화면에 뜨는 몽은 서버에 없는 임시 몽이다. 계정에 몽이 없어도 "이런 게 있다" 를
 * 보여 주려는 것이라, 이 화면은 서버를 전혀 부르지 않는다.
 */
@HiltViewModel
class GuideViewModel @Inject constructor(
    private val getInitGuideOpenOptionUseCase: GetInitGuideOpenOptionUseCase,
    private val setInitGuideOpenOptionUseCase: SetInitGuideOpenOptionUseCase,
) : BaseViewModel() {

    /**
     * 스테이지에서 밝힐 대상
     */
    enum class GuideTarget {
        MONG,
        CARE,
        MENU,

        /** 마지막 단계. 아무것도 흐리지 않고 전부 밝힌다. */
        NONE,
    }

    /**
     * 가이드 단계. 순서가 곧 진행 순서다.
     */
    enum class GuideStep(
        val target: GuideTarget,
        val title: String,
        val description: String,
    ) {
        MONG(
            target = GuideTarget.MONG,
            title = "몽",
            description = "함께 키우는 캐릭터예요\n눌러서 돌봐 주세요",
        ),
        CARE(
            target = GuideTarget.CARE,
            title = "돌보기",
            description = "밥 주기·쓰다듬기\n재우기를 할 수 있어요",
        ),
        MENU(
            target = GuideTarget.MENU,
            title = "메뉴",
            description = "미션·캐릭터 관리는\n옆 화면에 있어요",
        ),
        START(
            target = GuideTarget.NONE,
            title = "준비 끝",
            description = "첫 몽을 분양받아\n시작해 볼까요?",
        ),
        ;
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle : UiEvent()
        data object Finish : UiEvent()
    }

    /**
     * UI 이벤트 변수
     */
    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent: Flow<UiEvent> = _uiEvent.receiveAsFlow()

    /**
     * 변수
     */

    /**
     * 최초 가이드를 띄워야 하는지 여부. null 은 아직 조회 중이라는 뜻이고,
     * Router 가 시작 경로를 고르기 전에 이 값을 기다린다.
     */
    private val _initGuideOpen = MutableStateFlow<Boolean?>(null)
    val initGuideOpen: StateFlow<Boolean?> = _initGuideOpen.asStateFlow()

    private val _stepIndex = MutableStateFlow(0)
    val stepIndex: StateFlow<Int> = _stepIndex.asStateFlow()

    private val _currentStep = MutableStateFlow(GuideStep.entries.first())
    val currentStep: StateFlow<GuideStep> = _currentStep.asStateFlow()

    val stepCount: Int = GuideStep.entries.size

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _initGuideOpen.value = withContext(Dispatchers.IO) {
                getInitGuideOpenOptionUseCase()
            }
        }
    }

    /**
     * 다음 단계. 마지막 단계에서 누르면 가이드를 끝낸다.
     */
    fun next() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            if (_stepIndex.value >= stepCount - 1) {
                this@GuideViewModel.finish()
                return@launch
            }

            _stepIndex.value = min(_stepIndex.value + 1, stepCount - 1)
            _currentStep.value = GuideStep.entries[_stepIndex.value]
        }
    }

    /**
     * 가이드 종료.
     *
     * 도움말에서 다시 열어 끝낸 경우에도 같은 값을 한 번 더 쓸 뿐이라 문제 없다.
     */
    fun finish() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                setInitGuideOpenOptionUseCase(
                    command = SetInitGuideOpenOptionUseCase.Command(isOpen = false)
                )
            }

            _uiEvent.send(UiEvent.Finish)
        }
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _stepIndex.value = 0
            _currentStep.value = GuideStep.entries.first()
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        // 가이드가 막히면 앱 자체를 못 쓰게 되므로, 실패해도 메인으로는 보내 준다.
        _uiEvent.send(UiEvent.Finish)
    }
}

package com.monglife.mongs.presentation.viewmodel.pages.battle

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.battle.exception.NotFoundMatchRewardException
import com.monglife.mongs.application.battle.usecase.CreateMatchQueueUseCase
import com.monglife.mongs.application.battle.usecase.DeleteMatchQueueUseCase
import com.monglife.mongs.application.battle.usecase.GetMatchRewardUseCase
import com.monglife.mongs.application.battle.usecase.ObserveMatchQueueUseCase
import com.monglife.mongs.application.battle.vo.MatchQueueVo
import com.monglife.mongs.application.battle.vo.MatchRewardVo
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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

@HiltViewModel
class BattleMenuViewModel @Inject constructor(
    private val getMatchRewardUseCase: GetMatchRewardUseCase,
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val observeMatchQueueUseCase: ObserveMatchQueueUseCase,
    private val createMatchQueueUseCase: CreateMatchQueueUseCase,
    private val deleteMatchQueueUseCase: DeleteMatchQueueUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val matchingLoadingBar: Boolean = false,
        val deleteQueueConfirmDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Matching : UiState(matchingLoadingBar = true)
        data object DeleteQueueConfirm: UiState(matchingLoadingBar = true, deleteQueueConfirmDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class MatchingError(val message: String): UiEvent()
        data class NavMatch(val matchId: Long, val playerId: String): UiEvent()
        data class NavMain(val message: String): UiEvent()
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * UI 이벤트 변수
     *
     * SharedFlow 가 아니라 Channel 이다. SharedFlow 는 replay 가 0 이면 <b>구독자가 없는 순간
     * emit 한 값을 그냥 버린다</b> - 예외도 로그도 없다. 매칭 성공(NavMatch)이 그렇게 사라지면
     * 화면이 대기열에 그대로 머물고, 서버에는 매치가 만들어져 있어 참가비만 나간다.
     *
     * replay 를 주는 것으로는 못 고친다. 일회성 이동 이벤트라 새 구독자가 붙을 때마다 지난
     * 이동이 다시 재생돼 엉뚱한 화면으로 튄다. Channel 은 구독자가 없으면 버퍼에 담아 두고
     * 붙는 순간 정확히 한 번 넘겨준다.
     */
    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent: Flow<UiEvent> = _uiEvent.receiveAsFlow()

    /**
     * 변수
     */
    private val _matchRewardVo = MutableStateFlow<MatchRewardVo?>(null)
    val matchRewardVo: StateFlow<MatchRewardVo?> = _matchRewardVo.asStateFlow()

    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    private val _matchQueueVo = MutableStateFlow<MatchQueueVo?>(null)
    val matchQueueVo: StateFlow<MatchQueueVo?> = _matchQueueVo.asStateFlow()

    private var observeKey: String? = null

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _matchRewardVo.value = getMatchRewardUseCase()
                _currentMongVo.value = getCurrentMongUseCase()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 매칭 큐 등록
     */
    fun createQueue(mongId: Long) {
        if (_uiState.value != UiState.Matching) {
            viewModelScopeWithHandler.launch(Dispatchers.Main) {
                _uiState.value = UiState.Loading

                withContext(Dispatchers.IO) {
                    observeKey =  observeForever(
                        observeMatchQueueUseCase(
                            command = ObserveMatchQueueUseCase.Command(
                                mongId = mongId,
                            )
                        ), _matchQueueVo
                    )

                    createMatchQueueUseCase(
                        command = CreateMatchQueueUseCase.Command(mongId = mongId)
                    )
                }

                _uiState.value = UiState.Matching
            }
        }
    }

    /**
     * 매칭 큐 삭제
     */
    fun deleteQueue(mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                deleteMatchQueueUseCase(
                    command = DeleteMatchQueueUseCase.Command(
                        mongId = mongId,
                    )
                )

                observeKey?.let { observeStop(key = it) }
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 매칭 성공
     */
    fun matching(matchId: Long, playerId: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            if (playerId.isBlank()) {
                _uiEvent.send(UiEvent.MatchingError("매칭 정보 오류"))
            } else {
                _uiState.value = UiState.Loading

                _uiEvent.send(UiEvent.NavMatch(matchId, playerId))

                _matchQueueVo.value = null

                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 매치 큐 삭제 확인 다이얼로그 오픈
     */
    fun deleteQueueConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.DeleteQueueConfirm
        }
    }

    /**
     * 매치 큐 삭제 확인 다이얼로그 닫기
     */
    fun deleteQueueConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Matching
        }
    }

    override fun onCleared() {
        CoroutineScope(Dispatchers.IO).launch {
            if (_uiState.value == UiState.Matching) {
                _currentMongVo.value?.let {
                    deleteMatchQueueUseCase(
                        command = DeleteMatchQueueUseCase.Command(
                            mongId = it.mongId,
                        )
                    )
                }
            }
        }
        super.onCleared()
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
            is NotFoundMongException -> _uiEvent.send(UiEvent.NavMain("잠시후 다시 시도"))
            is NotFoundMatchRewardException -> _uiEvent.send(UiEvent.NavMain("잠시후 다시 시도"))
            else -> initialize()
        }
    }
}
package com.monglife.mongs.presentation.viewmodel.pages.battle

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.battle.usecase.CreateMatchQueueUseCase
import com.monglife.mongs.application.battle.usecase.DeleteMatchQueueUseCase
import com.monglife.mongs.application.battle.usecase.GetMatchRewardUseCase
import com.monglife.mongs.application.battle.vo.MatchQueueVo
import com.monglife.mongs.application.battle.vo.MatchRewardVo
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
class BattleMenuViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val createMatchQueueUseCase: CreateMatchQueueUseCase,
    private val deleteMatchQueueUseCase: DeleteMatchQueueUseCase,
    private val getMatchRewardUseCase: GetMatchRewardUseCase,
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
        data class NavMatch(val message: String): UiEvent()
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
                    createMatchQueueUseCase(command = CreateMatchQueueUseCase.Command(mongId = mongId))
                        .let { observeKey = observeForever(it, _matchQueueVo) }
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
    fun matching() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading
            _uiEvent.emit(UiEvent.NavMatch("매칭 성공"))
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
}
package com.monglife.mongs.presentation.viewmodel.pages.battle

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.battle.usecase.EnterMatchUseCase
import com.monglife.mongs.application.battle.usecase.ExitMatchUseCase
import com.monglife.mongs.application.battle.usecase.GetMatchRewardUseCase
import com.monglife.mongs.application.battle.usecase.GetWinnerMatchPlayerUseCase
import com.monglife.mongs.application.battle.usecase.ObserveMatchUseCase
import com.monglife.mongs.application.battle.usecase.PickMatchUseCase
import com.monglife.mongs.application.battle.vo.MatchRewardVo
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.application.battle.vo.WinnerMatchPlayerVo
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.battle.enums.MatchPickCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class BattleMatchViewModel @Inject constructor(
    private val getMatchRewardUseCase: GetMatchRewardUseCase,
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val observeMatchUseCase: ObserveMatchUseCase,
    private val enterMatchUseCase: EnterMatchUseCase,
    private val pickMatchUseCase: PickMatchUseCase,
    private val exitMatchUseCase: ExitMatchUseCase,
    private val getWinnerMatchPlayerUseCase: GetWinnerMatchPlayerUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val enteringLoadingBar: Boolean = false,
        val pickDialogOpen: Boolean = false,
        val endDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Entering : UiState(enteringLoadingBar = true)
        data object Pick : UiState(pickDialogOpen = true)
        data object End : UiState(endDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle : UiEvent()
        data class NavMenu(val message: String = ""): UiEvent()
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

    private val _matchVo = MutableStateFlow<MatchVo?>(null)
    val matchVo: StateFlow<MatchVo?> = _matchVo.asStateFlow()

    private val _matchPlayerVo = MutableStateFlow<MatchVo.MatchPlayerVo?>(null)
    val matchPlayerVo: StateFlow<MatchVo.MatchPlayerVo?> = _matchPlayerVo.asStateFlow()

    private val _matchPlayerMaxHp = MutableStateFlow(0f)
    val matchPlayerMaxHp: StateFlow<Float> = _matchPlayerMaxHp.asStateFlow()

    private val _targetMatchPlayerVo = MutableStateFlow<MatchVo.MatchPlayerVo?>(null)
    val targetMatchPlayerVo: StateFlow<MatchVo.MatchPlayerVo?> = _targetMatchPlayerVo.asStateFlow()

    private val _targetMatchPlayerMaxHp = MutableStateFlow(0f)
    val targetMatchPlayerMaxHp: StateFlow<Float> = _targetMatchPlayerMaxHp.asStateFlow()

    private val _winMatchPlayerVo = MutableStateFlow<WinnerMatchPlayerVo?>(null)
    val winMatchPlayerVo: StateFlow<WinnerMatchPlayerVo?> = _winMatchPlayerVo.asStateFlow()


    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _matchRewardVo.value = getMatchRewardUseCase()
                _currentMongVo.value = getCurrentMongUseCase()
            }

            _uiState.value = UiState.Entering
        }
    }

    /**
     * 매치 입장
     */
    fun enter(matchId: Long?, playerId: String?) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            if (matchId == null || playerId == null) {
                _uiEvent.emit(UiEvent.NavMenu("매칭 실패"))
                return@launch
            }

            withContext(Dispatchers.IO) {
                observeMatchUseCase(
                    command = ObserveMatchUseCase.Command(
                        matchId = matchId,
                        playerId = playerId,
                    )
                ).let { flow ->
                    observeForever(flow, _matchVo)

                    observeForever(flow.map { matchVo -> matchVo?.matchPlayers?.first { it.isMe } }, _matchPlayerVo)
                    _matchPlayerMaxHp.value = _matchPlayerVo.value?.hp?.toFloat() ?: 0f

                    observeForever(flow.map { matchVo -> matchVo?.matchPlayers?.first { !it.isMe } }, _targetMatchPlayerVo)
                    _targetMatchPlayerMaxHp.value = _targetMatchPlayerVo.value?.hp?.toFloat() ?: 0f
                }

                delay(Duration.ofSeconds(2).toMillis())

                enterMatchUseCase(
                    command = EnterMatchUseCase.Command(
                        matchId = matchId,
                        playerId = playerId,
                    )
                )
            }
        }
    }

    /**
     * 매치 라운드 변경
     */
    fun nextRound() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            _uiState.value = UiState.Idle

            delay(Duration.ofSeconds(2).toMillis())

            _uiState.value = UiState.Pick
        }
    }

    /**
     * 매치 선택
     */
    fun pick(matchId: Long, playerId: String, targetPlayerId: String, pickCode: MatchPickCode) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                pickMatchUseCase(
                    command = PickMatchUseCase.Command(
                        matchId = matchId,
                        playerId = playerId,
                        targetPlayerId = targetPlayerId,
                        pickCode = pickCode,
                    )
                )
            }

            _uiState.value = UiState.Idle

            // 매치 종료
            _matchVo.value?.let {
                if (it.isLastRound) {
                    delay(Duration.ofSeconds(2).toMillis())

                    _uiState.value = UiState.Loading

                    withContext(Dispatchers.IO) {
                        _winMatchPlayerVo.value = getWinnerMatchPlayerUseCase(
                            command = GetWinnerMatchPlayerUseCase.Command(
                                matchId = matchId
                            )
                        )
                    }

                    _uiState.value = UiState.End

                }
            }
        }
    }

    /**
     * 매치 퇴장
     */
    fun exit() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            _uiEvent.emit(UiEvent.NavMenu())
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
            if (_uiState.value != UiState.End) {
                // 매치 퇴장
                _matchVo.value?.let { matchVo ->
                    matchVo.matchPlayers.find { it.isMe }?.let { matchPlayer ->
                        exitMatchUseCase(
                            command = ExitMatchUseCase.Command(
                                matchId = matchVo.matchId,
                                playerId = matchPlayer.playerId,
                            )
                        )
                    }
                }
            }
        }
    }
}
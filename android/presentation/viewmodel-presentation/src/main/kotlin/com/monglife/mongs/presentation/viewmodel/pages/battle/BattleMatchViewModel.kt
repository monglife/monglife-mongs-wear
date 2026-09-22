package com.monglife.mongs.presentation.viewmodel.pages.battle

import android.util.Log
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchEnterException
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchPickException
import com.monglife.mongs.application.battle.usecase.EnterMatchUseCase
import com.monglife.mongs.application.battle.usecase.ExitMatchUseCase
import com.monglife.mongs.application.battle.usecase.GetWinnerMatchPlayerUseCase
import com.monglife.mongs.application.battle.usecase.ObserveMatchUseCase
import com.monglife.mongs.application.battle.usecase.PickMatchUseCase
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.application.battle.vo.WinnerMatchPlayerVo
import com.monglife.mongs.domain.battle.enums.MatchPickCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class BattleMatchViewModel @Inject constructor(
    private val observeMatchUseCase: ObserveMatchUseCase,
    private val enterMatchUseCase: EnterMatchUseCase,
    private val pickMatchUseCase: PickMatchUseCase,
    private val exitMatchUseCase: ExitMatchUseCase,
    private val getWinnerMatchPlayerUseCase: GetWinnerMatchPlayerUseCase,
): BaseViewModel() {

    companion object {
        private const val TAG = "BattleMatch"
        private const val MAX_SECONDS = 30
        private const val EFFECT_DELAY = 2000L
        private const val MAX_ROUND = 10

        /**
         * 매치 퇴장 발행 상한.
         *
         * MQTT 발행은 브로커 응답까지 suspend 한다. 화면을 떠나는 길목이라 응답이 늦으면
         * 아무 말 없이 영영 매달려 있게 되는데, 그러면 서버는 이탈을 모른 채 매치를
         * PROCESS 로 남기고 상대는 끝까지 기다린다. 실기기에서 실제로 이 모양이었다.
         */
        private const val EXIT_TIMEOUT = 5000L

        /**
         * 퇴장 발행 전용 스코프.
         *
         * viewModelScope 는 onCleared 시점에 이미 취소돼 쓸 수 없다. 예전에는 여기서
         * CoroutineScope(Dispatchers.IO) 를 매번 새로 만들었는데, SupervisorJob 이 없어
         * 발행이 던지면 처리되지 않은 예외로 올라갔다.
         */
        private val exitScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val enteringLoadingBar: Boolean = false,
        val pickDialogOpen: Boolean = false,
        val pickWaitingLoadingBar: Boolean = false,
        val endDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Entering : UiState(enteringLoadingBar = true)
        data object Pick : UiState(pickDialogOpen = true)
        data object PickWaiting: UiState(pickWaitingLoadingBar = true)
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
    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent: Flow<UiEvent> = _uiEvent.receiveAsFlow()

    /**
     * 변수
     */
    private val _matchVo = MutableStateFlow<MatchVo?>(null)
    val matchVo: StateFlow<MatchVo?> = _matchVo.asStateFlow()

    private val _matchPlayerVo = MutableStateFlow<MatchVo.MatchPlayerVo?>(null)
    val matchPlayerVo: StateFlow<MatchVo.MatchPlayerVo?> = _matchPlayerVo.asStateFlow()

    private val _matchPlayerMaxHp = MutableStateFlow(Float.MAX_VALUE)
    val matchPlayerMaxHp: StateFlow<Float> = _matchPlayerMaxHp.asStateFlow()

    private val _targetMatchPlayerVo = MutableStateFlow<MatchVo.MatchPlayerVo?>(null)
    val targetMatchPlayerVo: StateFlow<MatchVo.MatchPlayerVo?> = _targetMatchPlayerVo.asStateFlow()

    private val _targetMatchPlayerMaxHp = MutableStateFlow(Float.MAX_VALUE)
    val targetMatchPlayerMaxHp: StateFlow<Float> = _targetMatchPlayerMaxHp.asStateFlow()

    private val _winMatchPlayerVo = MutableStateFlow<WinnerMatchPlayerVo?>(null)
    val winMatchPlayerVo: StateFlow<WinnerMatchPlayerVo?> = _winMatchPlayerVo.asStateFlow()

    private val _maxRound = MutableStateFlow(MAX_ROUND)
    val maxRound: StateFlow<Int> = _maxRound.asStateFlow()

    private val _maxSeconds = MutableStateFlow(MAX_SECONDS)
    val maxSeconds: StateFlow<Int> = _maxSeconds.asStateFlow()

    /** 퇴장을 이미 처리했는지. dispose 와 onCleared 가 겹쳐도 한 번만 보낸다. */
    private var exitPublished = false

    /**
     * 화면 진입 인자. 퇴장 발행의 최후 수단이다.
     *
     * 매치 정보(_matchVo)는 첫 MQTT 메시지가 와야 채워지는데, 그 전에 이탈하는 구간
     * ("매치입장중")이 실제로 제일 잦다. 그때 퇴장을 못 보내면 서버는 PROCESS 로 남긴다.
     */
    private var enteredMatchId: Long? = null
    private var enteredPlayerId: String? = null

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Entering
        }
    }

    /**
     * 매치 입장
     */
    fun enter(matchId: Long?, playerId: String?) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            if (matchId == null || playerId == null) {
                _uiEvent.send(UiEvent.NavMenu("매칭 실패"))
                return@launch
            }

            enteredMatchId = matchId
            enteredPlayerId = playerId

            withContext(Dispatchers.IO) {
                observeMatchUseCase(
                    command = ObserveMatchUseCase.Command(
                        matchId = matchId,
                        playerId = playerId,
                    )
                ).let { flow ->
                    /**
                     * 원본 flow 는 한 번만 구독한다.
                     * 이전에는 파생 상태마다 observeForever 를 걸어 같은 cold flow 를 3번 구독했고,
                     * 그 결과 MQTT 구독과 Room 쿼리가 3배로 발생했다.
                     */
                    var maxHpInitialized = false

                    observeForever(flow) { matchVo ->
                        _matchVo.value = matchVo

                        val me = matchVo?.matchPlayers?.firstOrNull { it.isMe }
                        val target = matchVo?.matchPlayers?.firstOrNull { !it.isMe }

                        _matchPlayerVo.value = me
                        _targetMatchPlayerVo.value = target

                        // 최대 HP 는 첫 수신 값으로 한 번만 고정한다 (기존 동작과 동일)
                        if (!maxHpInitialized) {
                            maxHpInitialized = true
                            _matchPlayerMaxHp.value = me?.hp ?: 5000f
                            _targetMatchPlayerMaxHp.value = target?.hp ?: 5000f
                        }
                    }
                }

                delay(EFFECT_DELAY)

                // 매치 입장
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

            delay(EFFECT_DELAY)

            _uiState.value = UiState.Pick
        }
    }

    /**
     * 매치 종료
     */
    fun end(matchId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {

            _uiState.value = UiState.Idle

            delay(EFFECT_DELAY)

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

    /**
     * 매치 선택
     */
    fun pick(matchId: Long, playerId: String, targetPlayerId: String, pickCode: MatchPickCode) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle

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

            _uiState.value = UiState.PickWaiting
        }
    }

    /**
     * 매치 퇴장
     */
    fun exit() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading
            _uiEvent.send(UiEvent.NavMenu())
        }
    }

    /**
     * 화면을 떠날 때 매치 퇴장을 알린다.
     *
     * 안 보내면 서버는 플레이어가 나간 걸 모른 채 매치를 PROCESS 로 남기고, 상대는 끝까지
     * 기다린다. 입장 기한 초과 스위퍼는 ENTERING 만 걷어가므로 이 상태는 아무도 치우지 않는다.
     *
     * <b>onCleared 가 아니라 화면 dispose 에서 부른다.</b> SwipeDismissableNavHost 는 pop 한
     * 엔트리의 ViewModelStore 를 정리하지 않아 onCleared 가 오지 않는다 - 실기기에서 뒤로가기
     * 뒤 72초를 기다려도 불리지 않았고, 그렇게 빠져나간 매치 3건이 PROCESS 로 남았다.
     *
     * 한 번만 보낸다. dispose 와 onCleared 가 모두 오는 경우가 있어도 중복 발행하지 않는다.
     */
    fun leaveMatch() {

        if (exitPublished) return

        exitPublished = true

        publishExitIfAbandoned()
    }

    override fun onCleared() {
        leaveMatch()
        super.onCleared()
    }

    /**
     * 상태 읽기는 여기서(=취소 전) 끝내고 발행만 별도 스코프에 넘긴다.
     * 코루틴 안에서 읽으면 ViewModel 이 정리된 뒤의 값을 볼 수 있다.
     */
    private fun publishExitIfAbandoned() {
        val matchVo = _matchVo.value

        // 정상 종료다. 보낼 필요가 없다.
        if (_uiState.value == UiState.End || matchVo?.isLastRound == true) return

        // 매치 정보가 아직 없어도 보낸다 - 진입 인자로 폴백한다.
        val matchId = matchVo?.matchId ?: enteredMatchId
        val playerId = matchVo?.matchPlayers?.find { it.isMe }?.playerId ?: enteredPlayerId

        if (matchId == null || playerId == null) {
            Log.w(TAG, "매치 퇴장 건너뜀 - 매치를 특정할 수 없다 matchId=$matchId")
            return
        }

        exitScope.launch {
            runCatching {
                withTimeout(EXIT_TIMEOUT) {
                    exitMatchUseCase(
                        command = ExitMatchUseCase.Command(
                            matchId = matchId,
                            playerId = playerId,
                        )
                    )
                }
            }.onFailure {
                Log.w(TAG, "매치 퇴장 발행 실패 matchId=$matchId", it)
            }
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
        when (exception) {
            is InvalidPublishMatchEnterException -> _uiEvent.send(UiEvent.NavMenu("매치 입장 실패"))
            is InvalidPublishMatchPickException -> _uiState.value = UiState.Pick
            else -> initialize()
        }
    }
}
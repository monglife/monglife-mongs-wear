package com.monglife.mongs.presentation.viewmodel.pages.mission

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.exception.InvalidClaimMissionRewardException
import com.monglife.mongs.application.mong.exception.NotFoundMissionException
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.mission.ClaimMissionRewardUseCase
import com.monglife.mongs.application.mong.usecase.mission.GetMissionsUseCase
import com.monglife.mongs.application.mong.vo.MissionVo
import com.monglife.mongs.application.mong.vo.MongVo
import dagger.hilt.android.lifecycle.HiltViewModel
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

/**
 * 미션 ViewModel
 *
 * 미션 그래프(mission)에 스코프를 걸어 메뉴·목록·상세 세 화면이 한 인스턴스를 공유한다.
 * 서버에 단건 조회 API 가 없고 목록 한 번이 일간·주간·월간을 모두 담고 있어,
 * 화면을 넘나들 때마다 다시 부르지 않으려면 이 방법뿐이다.
 */
@HiltViewModel
class MissionViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val getMissionsUseCase: GetMissionsUseCase,
    private val claimMissionRewardUseCase: ClaimMissionRewardUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val confirmDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Confirm : UiState(confirmDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class NavMain(val message: String): UiEvent()
        data class Claim(val message: String): UiEvent()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _missionVos = MutableStateFlow<List<MissionVo>>(emptyList())
    val missionVos: StateFlow<List<MissionVo>> = _missionVos.asStateFlow()

    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                /**
                 * 몽이 없어도 미션 목록은 볼 수 있다. 진행도는 계정 단위로 쌓이고
                 * 몽이 필요한 것은 보상 수령뿐이라, 여기서 메인으로 돌려보내지 않는다.
                 */
                _currentMongVo.value = getCurrentMongUseCase()

                /**
                 * 구독을 걸어 두어야 수령 직후 서버가 MQTT 로 쏘는 몽 갱신
                 * (페이 포인트·경험치)이 화면에 그대로 들어온다.
                 */
                observeForever(observeCurrentMongUseCase(), _currentMongVo)

                this@MissionViewModel.updateMissionVos()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 보상 수령 확인 다이얼로그 열기
     */
    fun claimConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Confirm
        }
    }

    /**
     * 보상 수령 확인 다이얼로그 닫기
     */
    fun claimConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 미션 보상 수령
     */
    fun claimMissionReward(accountMissionId: Long, mongId: Long) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                claimMissionRewardUseCase(
                    command = ClaimMissionRewardUseCase.Command(
                        accountMissionId = accountMissionId,
                        mongId = mongId,
                    )
                )

                // 수령 후 상태(CLAIMED)를 화면에 반영하려면 목록을 다시 받아야 한다
                this@MissionViewModel.updateMissionVos()

                _uiEvent.emit(UiEvent.Claim("보상을 받았어요"))
            }

            _uiState.value = UiState.Idle
        }
    }

    private suspend fun updateMissionVos() {
        _missionVos.value = getMissionsUseCase()
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
            is NotFoundMissionException -> _uiEvent.emit(UiEvent.NavMain("잠시후 다시 시도"))
            is InvalidClaimMissionRewardException -> _uiState.value = UiState.Idle
            else -> initialize()
        }
    }
}

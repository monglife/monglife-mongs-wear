package com.monglife.mongs.presentation.viewmodel.pages.help

import com.monglife.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(

): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val detailDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Detail: UiState(detailDialogOpen = true)
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _currentHelpVo = MutableStateFlow<HelpVo?>(null)
    val currentHelpVo: StateFlow<HelpVo?> = _currentHelpVo.asStateFlow()

    private val _helpVos = MutableStateFlow<List<HelpVo>>(emptyList())
    val helpVos: StateFlow<List<HelpVo>> = _helpVos.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _helpVos.value = listOf(
                    HelpVo(
                        helpIconCode = "HP000",
                        title = "게임소개",
                        subTitle = "진화조건,졸업시기",
                        contents = listOf(
                            "캐릭터 레벨은",
                            "총 3단계까지 존재,",
                            "관심도에 따라",
                            "진화 가능한 캐릭터",
                            "종류가 달라져요.",
                            "\n",
                            "경험치를 채우면",
                            "몽 진화가 가능해요.",
                            "다양한 활동을 통해",
                            "경험치를 채워보세요"
                        ),
                    ),
                    HelpVo(
                        helpIconCode = "HP001",
                        title = "컬렉션",
                        subTitle = "몽/맵 컬렉션 수집",
                        contents = listOf(
                            "컬렉션이란?",
                            "수집한 몽 도감과",
                            "수집한 맵 도감을",
                            "컬렉션에서",
                            "확인할 수 있어요.",
                            "\n",
                            "몽 도감은 진화를 통해,",
                            "맵 도감은 뽑기를 통해",
                            "수집할 수 있어요."
                        ),
                    ),
                    HelpVo(
                        helpIconCode = "HP002",
                        title = "뽑기",
                        subTitle = "인벤토리 아이템 뽑기",
                        contents = listOf(
                            "뽑기란?",
                            "페이포인트를 소비하여",
                            "랜덤으로 인벤토리",
                            "아이템을 뽑을 수 있어요.",
                            "\n",
                            "인벤토리 아이템은",
                            "맵 도감/밥/간식으로",
                            "이뤄져 있어요.",
                            "\n",
                            "맵 도감은 도감에 등록,",
                            "밥/간식은 인벤토리에",
                            "저장되고 인벤토리에서",
                            "소비할 수 있어요."
                        ),
                    ),
                    HelpVo(
                        helpIconCode = "HP003",
                        title = "훈련",
                        subTitle = "달리기/농구 미니게임",
                        contents = listOf(
                            "훈련이란?",
                            "미니게임을 통해",
                            "캐릭터의 페이포인트를",
                            "채울 수 있어요.",
                            "\n",
                            "훈련은 일정 스텟을",
                            "차감하여 플레이하며",
                            "목표 점수 달성 시,",
                            "페이 포인트를 보상받아요.",
                        ),
                    ),
                    HelpVo(
                        helpIconCode = "HP004",
                        title = "배틀",
                        subTitle = "매칭,매치,보상",
                        contents = listOf(
                            "배틀이란?",
                            "플레이어와 겨루는 게임",
                            "\n",
                            "배틀은 페이포인트를 소비하여",
                            "매치를 잡을 수 있어요.",
                            "\n",
                            "플레이어와 실시간으로",
                            "배틀 매칭이 진행되고,",
                            "\n",
                            "플레이어가 없는 경우",
                            "봇이 매칭되어 진행되요.",
                            "\n",
                            "배틀 매치는",
                            "공격/방어/회복을 선택,",
                            "상대의 HP를 깎으면",
                            "승리해요.",
                            "\n",
                            "라운드 내에",
                            "매치가 종료 되지 않으면",
                            "상대적으로 HP가 높은",
                            "플레이어가 승리해요.",
                            "\n",
                            "방어 - HP 소량 감소",
                            "공격 - 상대 HP 감소",
                            "회복 - HP 대량 증가",
                            "\n",
                            "승리한 플레이어는",
                            "페이포인트 보상받아요.",
                            "패배한 플레이어는",
                            "보상이 없어요."
                        )
                    ),
                    HelpVo(
                        helpIconCode = "HP005",
                        title = "캐릭터 키우기",
                        subTitle = "몽 상호작용",
                        contents = listOf(
                            "캐릭터와 다양한 상호작용을",
                            "통해 몽을 키울 수 있어요.",
                            "\n",
                            "캐릭터를 클릭해 상호작용",
                            "메뉴를 열 수 있어요.",
                            "\n",
                            "하트 버튼을 통해  몽을",
                            "쓰다듬을 수 있고,",
                            "경험치가 증가해요.",
                            "\n",
                            "포크 버튼을 클릭해 밥/간식",
                            "스텟 증가량 확인이 가능하고,",
                            "밥/간식을 섭취하여 스텟을",
                            "증가시킬 수 있어요.",
                            "\n",
                            "침대 버튼을 클릭해",
                            "수면 상태 변경이 가능해요.",
                            "수면 상태이면 기상 상태로",
                            "기상 상태이면 수면 상태로",
                            "변경되요.",
                            "\n",
                            "수면 상태에는",
                            "체력/포만감이 증가하고,",
                            "기상 상태에는",
                            "체력/포만감/힘이 감소해요.",
                            "\n",
                            "배변 버튼을 클릭해",
                            "배변을 처리할 수 있어요.",
                            "배변 수에 비례해",
                            "경험치가 증가해요.",
                            "\n",
                            "가방 버튼을 클릭해",
                            "인벤토리 목록 확인이 가능하고",
                            "밥/간식을 소비할 수 있어요."
                        ),
                    ),
                    HelpVo(
                        helpIconCode = "HP006",
                        title = "캐릭터 관리",
                        subTitle = "생성,삭제,졸업,슬롯추가",
                        contents = listOf(
                            "캐릭터 관리 메뉴를 통해",
                            "몽을 관리할 수 있어요.",
                            "\n",
                            "생성 버튼을 클릭해",
                            "새로운 알 분영이 가능해요.",
                            "이름/수면/기상 시간을",
                            "입력해야 해요.",
                            "\n",
                            "분양 받은 알은 5분 이후",
                            "자동으로 부화해요.",
                            "\n",
                            "상호작용을 할 몽 선택,",
                            "정보 확인이 가능해요.",
                            "\n",
                            "삭제 버튼을 클릭해",
                            "몽을 삭제할 수 있어요.",
                            "삭제 시, 페이포인트와",
                            "인벤토리가 소멸해요.",
                            "\n",
                            "완전히 성장 시키면",
                            "졸업 버튼이 활성화되고,",
                            "몽을 졸업시킬 수 있어요.",
                            "졸업 시, 페이포인트와",
                            "인벤토리가 소멸해요.",
                            "\n",
                            "여러 캐릭터를 키울 수 있는",
                            "슬롯을 스타포인트를 통해",
                            "구매할 수 있어요."
                        ),
                    ),
                    HelpVo(
                        helpIconCode = "HP007",
                        title = "페이포인트 환전",
                        subTitle = "걸음 수 환전 및 스타포인트 환전",
                        contents = listOf(
                            "열심히 채운 걸음 수와",
                            "스타포인트를 페이포인트로",
                            "환전할 수 있어요.",
                            "\n",
                            "걸음 수는 1000걸음 당",
                            "페이포인트 100개로 환전",
                            "스타포인트는 1개 당",
                            "페이포인트 1000개로 환전",
                            "이 가능해요."
                        ),
                    ),
                    HelpVo(
                        helpIconCode = "HP007",
                        title = "스타포인트 충전",
                        subTitle = "인앱 포인트 구매",
                        contents = listOf(
                            "스타포인트 충전 메뉴로",
                            "스타포인트 구매가 가능해요.",
                            "\n",
                            "모바일 기기와 연동하여",
                            "구글 플레이 스토어를 통해",
                            "구매가 가능해요.",
                            "\n",
                            "스타포인트는 ",
                            "추가 캐릭터 슬롯 구매, ",
                            "페이포인트 환전에",
                            "소비 가능해요."
                        ),
                    ),
                )
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 도움말 다이얼로그 오픈
     */
    fun helpDialogOpen(helpVo: HelpVo) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _currentHelpVo.value = helpVo
            _uiState.value = UiState.Detail
        }
    }

    /**
     * 도움말 다이얼로그 닫기
     */
    fun helpDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
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
     * 도움말 정보 Vo
     */
    data class HelpVo(
        val helpIconCode: String? = null,
        val title: String,
        val subTitle: String,
        val contents: List<String>,
    )
}
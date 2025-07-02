package com.monglife.mongs.presentation.viewmodel.pages.feed

import com.monglife.mongs.application.mong.usecase.interaction.FeedFoodMongUseCase
import com.monglife.mongs.application.mong.usecase.interaction.GetFoodsUseCase
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.FoodVo
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.core.presentation.viewmodel.BaseViewModel
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

@HiltViewModel
class FeedFoodViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val getFoodsUseCase: GetFoodsUseCase,
    private val feedFoodMongUseCase: FeedFoodMongUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val detailDialogOpen: Boolean = false,
        val buyDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Detail: UiState(detailDialogOpen = true)
        data object Buy: UiState(buyDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class NavMenu(val message: String): UiEvent()
        data object Feed: UiEvent()
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
    private val _mongVo = MutableStateFlow<MongVo?>(null)
    val mongVo: StateFlow<MongVo?> = _mongVo.asStateFlow()

    private val _foodVos = MutableStateFlow<List<FoodVo>>(emptyList())
    val foodVos: StateFlow<List<FoodVo>> = _foodVos.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getCurrentMongUseCase()?.let {
                    _mongVo.value = it

                    // 음식 목록 조회
                    _foodVos.value = getFoodsUseCase(
                        command = GetFoodsUseCase.Command(
                            mongId = it.mongId,
                        )
                    )
                } ?: _uiEvent.emit(UiEvent.NavMenu("선택된 몽이 없음"))
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 먹이 섭취
     */
    fun buyFood(mongId: Long, foodCode: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                feedFoodMongUseCase(
                    command = FeedFoodMongUseCase.Command(
                        mongId = mongId,
                        foodCode = foodCode,
                    )
                )

                _uiEvent.emit(UiEvent.Feed)
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 상세 다이얼로그 오픈
     */
    fun detailDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Detail
        }
    }

    /**
     * 상세 다이얼로그 닫기
     */
    fun detailDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 구매 확인 다이얼로그 오픈
     */
    fun buyDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Buy
        }
    }

    /**
     * 구매 확인 다이얼로그 닫기
     */
    fun buyDialogClose() {
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
}
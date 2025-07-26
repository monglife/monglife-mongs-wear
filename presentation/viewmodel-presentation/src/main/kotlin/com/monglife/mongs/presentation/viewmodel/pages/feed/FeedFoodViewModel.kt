package com.monglife.mongs.presentation.viewmodel.pages.feed

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.mong.exception.InvalidFeedFoodException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.usecase.interaction.FeedFoodMongUseCase
import com.monglife.mongs.application.mong.usecase.interaction.GetFoodsUseCase
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
import com.monglife.mongs.application.mong.usecase.management.ObserveCurrentMongUseCase
import com.monglife.mongs.application.mong.vo.FoodVo
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
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class FeedFoodViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val getFoodsUseCase: GetFoodsUseCase,
    private val observeCurrentMongUseCase: ObserveCurrentMongUseCase,
    private val feedFoodMongUseCase: FeedFoodMongUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val detailDialogOpen: Boolean = false,
        val confirmDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Detail: UiState(detailDialogOpen = true)
        data object Confirm: UiState(confirmDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class NavMenu(val message: String): UiEvent()
        data object Buy: UiEvent()
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
    private val _currentMongVo = MutableStateFlow<MongVo?>(null)
    val currentMongVo: StateFlow<MongVo?> = _currentMongVo.asStateFlow()

    private val _foodVos = MutableStateFlow<List<FoodVo>>(emptyList())
    val foodVos: StateFlow<List<FoodVo>> = _foodVos.asStateFlow()

    private val _currentFoodVo = MutableStateFlow<FoodVo?>(null)
    val currentFoodVo: StateFlow<FoodVo?> = _currentFoodVo.asStateFlow()

    private val _foodVoIndex = MutableStateFlow(0)
    val foodVoIndex: StateFlow<Int> = _foodVoIndex.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getCurrentMongUseCase()?.let {
                    _currentMongVo.value = it

                    // 음식 목록 조회
                    _foodVos.value = getFoodsUseCase(
                        command = GetFoodsUseCase.Command(
                            mongId = it.mongId,
                        )
                    )

                    if (_foodVoIndex.value in 0..< _foodVos.value.size) {
                        _currentFoodVo.value = _foodVos.value[_foodVoIndex.value]
                    } else {
                        _currentFoodVo.value = null
                    }
                } ?: run {
                    _uiEvent.emit(UiEvent.NavMenu("선택된 몽이 없음"))
                    return@withContext
                }

                observeForever(observeCurrentMongUseCase(), _currentMongVo)
            }

            _uiState.value = UiState.Idle
        }
    }

    fun nextFood() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _foodVoIndex.value = min(_foodVoIndex.value + 1, _foodVos.value.size - 1)

            if (_foodVoIndex.value in 0..< _foodVos.value.size) {
                _currentFoodVo.value = _foodVos.value[_foodVoIndex.value]
            } else {
                _currentFoodVo.value = null
            }
        }
    }

    fun prevFood() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _foodVoIndex.value = max(_foodVoIndex.value - 1, 0)

            if (_foodVoIndex.value in 0..< _foodVos.value.size) {
                _currentFoodVo.value = _foodVos.value[_foodVoIndex.value]
            } else {
                _currentFoodVo.value = null
            }
        }
    }

    /**
     * 간식 섭취
     */
    fun buy(mongId: Long, foodCode: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                feedFoodMongUseCase(
                    command = FeedFoodMongUseCase.Command(
                        mongId = mongId,
                        foodCode = foodCode,
                    )
                )

                _uiEvent.emit(UiEvent.Buy)
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
    fun buyConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Confirm
        }
    }

    /**
     * 구매 확인 다이얼로그 닫기
     */
    fun buyConfirmDialogClose() {
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
        when (exception) {
            is NotFoundMongException -> _uiEvent.emit(UiEvent.NavMenu("잠시후 다시 시도"))
            is InvalidFeedFoodException -> _uiState.value = UiState.Idle
            else -> initialize()
        }
    }
}
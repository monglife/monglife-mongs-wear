package com.monglife.mongs.presentation.viewmodel.pages.collection

import com.monglife.mongs.application.device.usecase.SetBackgroundMapCodeUseCase
import com.monglife.mongs.application.member.collection.usecase.GetCollectionMapsUseCase
import com.monglife.mongs.application.member.collection.vo.CollectionMapVo
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
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
class CollectionMapViewModel @Inject constructor(
    private val getCollectionMapsUseCase: GetCollectionMapsUseCase,
    private val setBackgroundMapCodeUseCase: SetBackgroundMapCodeUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val collectionMapDetailDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Detail : UiState(collectionMapDetailDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class SetBackground(val message: String): UiEvent()
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
    private val _collectionMapVos = MutableStateFlow<List<CollectionMapVo>>(emptyList())
    val collectionMapVos: StateFlow<List<CollectionMapVo>> = _collectionMapVos.asStateFlow()

    private val _detailCollectionMapVo = MutableStateFlow<CollectionMapVo?>(null)
    val detailCollectionMapVo: StateFlow<CollectionMapVo?> = _detailCollectionMapVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _collectionMapVos.value = getCollectionMapsUseCase()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 맵 컬렉션 상세 다이얼로그 오픈
     */
    fun collectionMapDetailDialogOpen(collectionMapVo: CollectionMapVo) {
        viewModelScopeWithHandler.launch (Dispatchers.Main) {
            _detailCollectionMapVo.value = collectionMapVo
            _uiState.value = UiState.Detail
        }
    }

    /**
     * 맵 컬렉션 상세 다이얼로그 닫기
     */
    fun collectionMapDetailDialogClose() {
        viewModelScopeWithHandler.launch (Dispatchers.Main) {
            _uiState.value = UiState.Idle
            _detailCollectionMapVo.value = null
        }
    }

    /**
     * 배경 설정
     */
    fun setBackgroundMapCode(mapCode: String) {
        viewModelScopeWithHandler.launch (Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                setBackgroundMapCodeUseCase(
                    command = SetBackgroundMapCodeUseCase.Command(
                        mapCode = mapCode,
                    )
                )
            }

            _uiEvent.emit(UiEvent.SetBackground("설정 완료"))

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch (Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        initialize()
    }
}
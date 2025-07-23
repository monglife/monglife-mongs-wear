package com.monglife.mongs.presentation.viewmodel.pages.collection

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.member.collection.usecase.GetCollectionMongsUseCase
import com.monglife.mongs.application.member.collection.vo.CollectionMongVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
class CollectionMongViewModel @Inject constructor(
    private val getCollectionMongsUseCase: GetCollectionMongsUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val collectionMongDetailDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Detail : UiState(collectionMongDetailDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class NavMenu(val message: String): UiEvent()
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
    private val _collectionMongVos = MutableStateFlow<List<CollectionMongVo>>(emptyList())
    val collectionMongVos: StateFlow<List<CollectionMongVo>> = _collectionMongVos.asStateFlow()

    private val _detailCollectionMongVo = MutableStateFlow<CollectionMongVo?>(null)
    val detailCollectionMongVo: StateFlow<CollectionMongVo?> = _detailCollectionMongVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getCollectionMongsUseCase().let {
                    if (it.isNotEmpty()) {
                        _collectionMongVos.value = it
                    } else {
                        delay(NAVIGATE_DELAY)
                        _uiEvent.emit(UiEvent.NavMenu("몽 컬렉션 없음"))
                    }
                }
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 몽 컬렉션 상세 다이얼로그 오픈
     */
    fun collectionMongDetailDialogOpen(collectionMongVo: CollectionMongVo) {
        viewModelScopeWithHandler.launch (Dispatchers.Main) {
            _detailCollectionMongVo.value = collectionMongVo
            _uiState.value = UiState.Detail
        }
    }

    /**
     * 몽 컬렉션 상세 다이얼로그 닫기
     */
    fun collectionMongDetailDialogClose() {
        viewModelScopeWithHandler.launch (Dispatchers.Main) {
            _uiState.value = UiState.Idle
            _detailCollectionMongVo.value = null
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
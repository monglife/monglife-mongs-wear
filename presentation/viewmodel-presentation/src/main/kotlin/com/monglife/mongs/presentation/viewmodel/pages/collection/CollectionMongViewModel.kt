package com.monglife.mongs.presentation.viewmodel.pages.collection

import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.member.collection.usecase.GetCollectionMongsUseCase
import com.monglife.mongs.application.member.collection.vo.CollectionMongVo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
                _collectionMongVos.value = getCollectionMongsUseCase()
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
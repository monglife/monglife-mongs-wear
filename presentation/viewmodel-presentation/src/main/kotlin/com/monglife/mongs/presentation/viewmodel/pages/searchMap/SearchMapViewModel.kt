package com.monglife.mongs.presentation.viewmodel.pages.searchMap

import com.monglife.core.presentation.utils.PermissionUtil
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.member.collection.exception.InvalidSearchCollectionMapException
import com.monglife.mongs.application.member.collection.usecase.SearchCollectionMapUseCase
import com.monglife.mongs.application.member.collection.vo.CollectionMapVo
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.usecase.management.GetCurrentMongUseCase
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
class SearchMapViewModel @Inject constructor(
    private val getCurrentMongUseCase: GetCurrentMongUseCase,
    private val searchCollectionMapUseCase: SearchCollectionMapUseCase,
    private val permissionUtil: PermissionUtil,
): BaseViewModel() {

    companion object {
        private const val SEARCH_DELAY = 2000L
    }

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val confirmDialogOpen: Boolean = false,
        val searchLoading: Boolean = false,
        val searchDetailDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Confirm : UiState(confirmDialogOpen = true)
        data object Search: UiState(searchLoading = true)
        data object Detail: UiState(searchDetailDialogOpen = true)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class NavMain(val message: String): UiEvent()
        data class NotFound(val message: String): UiEvent()
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
    private val _permission = MutableStateFlow(false)
    val permission: StateFlow<Boolean> = _permission.asStateFlow()

    private val _collectionMapVo = MutableStateFlow<CollectionMapVo?>(null)
    val collectionMapVo: StateFlow<CollectionMapVo?> = _collectionMapVo.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _permission.value = permissionUtil.verifyLocationPermission().isEmpty()

                getCurrentMongUseCase() ?: run {
                    _uiEvent.emit(UiEvent.NavMain("선택된 몽이 없음"))
                    return@withContext
                }
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 맵 탐색
     */
    fun searchMap() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Search

            delay(SEARCH_DELAY)

            withContext(Dispatchers.IO) {
                searchCollectionMapUseCase()?.let {
                    _collectionMapVo.value = it
                }
            }

            _collectionMapVo.value?.let {
                _uiState.value = UiState.Detail
            } ?: run {
                _uiEvent.emit(UiEvent.NotFound("탐색된 맵이 없음"))
                _uiState.value = UiState.Idle
            }
        }
    }

    /**
     * 맵 탐색 확인 다이얼로그 오픈
     */
    fun searchMapConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Confirm
        }
    }

    /**
     * 맵 탐색 확인 다이얼로그 닫기
     */
    fun searchMapConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 맵 탐색 결과 다이얼로그 닫기
     */
    fun searchMapDetailDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 위치 권한 체크
     */
    fun verifyLocationPermission() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            _permission.value = permissionUtil.verifyLocationPermission().isEmpty()
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
            is NotFoundMongException -> _uiEvent.emit(UiEvent.NavMain("잠시후 다시 시도"))
            is InvalidSearchCollectionMapException -> _uiState.value = UiState.Idle
            else -> initialize()
        }
    }
}
package com.monglife.mongs.presentation.viewmodel.pages.notice

import com.monglife.mongs.application.member.notice.usecase.GetNoticesUseCase
import com.monglife.mongs.application.member.notice.vo.NoticeVo
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.presentation.viewmodel.pages.feedback.FeedbackViewModel
import com.monglife.mongs.presentation.viewmodel.pages.inventory.InventoryViewModel
import com.monglife.mongs.presentation.viewmodel.pages.inventory.InventoryViewModel.Companion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getNoticesUseCase: GetNoticesUseCase,
): BaseViewModel() {

    companion object {
        private const val INIT_PAGE = 1
        private const val INIT_SIZE = 4
    }

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val detailDialogOpen: Boolean = false,
        val listLoadingBar: Boolean = false
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object ListLoading : UiState(listLoadingBar = true)
        data object Detail : UiState(detailDialogOpen = true)
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _page = MutableStateFlow(INIT_PAGE)
    val page: StateFlow<Int> = _page.asStateFlow()

    private val _isLastPage = MutableStateFlow(true)
    val isLastPage: StateFlow<Boolean> = _isLastPage.asStateFlow()

    private val _noticeVos = MutableStateFlow<List<NoticeVo>>(emptyList())
    val noticeVos: StateFlow<List<NoticeVo>> = _noticeVos.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                getNoticesUseCase(
                    command = GetNoticesUseCase.Command(
                        page = INIT_PAGE,
                        size = INIT_SIZE,
                    )
                ).let { noticeVosPage ->
                    _page.value = noticeVosPage.page
                    _isLastPage.value = noticeVosPage.isLastPage
                    _noticeVos.value = noticeVosPage.result
                }
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 페이지 변경
     */
    fun updatePage(page: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            if (_uiState.value == UiState.ListLoading) {
                return@launch
            }

            _uiState.value = UiState.ListLoading

            withContext(Dispatchers.IO) {
                getNoticesUseCase(
                    command = GetNoticesUseCase.Command(
                        page = page,
                        size = INIT_SIZE,
                    )
                ).let { noticeVosPage ->
                    _page.value = noticeVosPage.page
                    _isLastPage.value = noticeVosPage.isLastPage
                    _noticeVos.value += noticeVosPage.result
                }
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 공지사항 상세 다이얼로그 오픈
     */
    fun noticeDetailDialogOpen(content: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _content.value = content
            _uiState.value = UiState.Detail
        }
    }

    /**
     * 공지사항 상세 다이얼로그 닫기
     */
    fun noticeDetailDialogClose() {
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
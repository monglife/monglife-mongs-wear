package com.monglife.mongs.presentation.viewmodel.pages.feedback

import com.monglife.mongs.application.member.feedback.usecase.CreateFeedbackUseCase
import com.monglife.mongs.application.member.feedback.usecase.GetFeedbackTypesUseCase
import com.monglife.mongs.application.member.feedback.vo.FeedbackTypeVo
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val getFeedbackTypesUseCase: GetFeedbackTypesUseCase,
    private val createFeedbackUseCase: CreateFeedbackUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val createDialogOpen: Boolean = false,
        val createConfirmDialogOpen: Boolean = false,
        val createSuccessDialogOpen: Boolean = false,
    ) {
        data object Idle : UiState()
        data object Loading : UiState(loadingBar = true)
        data object Create: UiState(createDialogOpen = true)
        data object CreateConfirm: UiState(createDialogOpen = true, createConfirmDialogOpen = true)
        data object CreateSuccess: UiState(createSuccessDialogOpen = true)
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 변수
     */
    private val _feedbackTypeVos = MutableStateFlow<List<FeedbackTypeVo>>(emptyList())
    val feedbackTypeVos: StateFlow<List<FeedbackTypeVo>> = _feedbackTypeVos.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    init {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                _feedbackTypeVos.value = getFeedbackTypesUseCase()
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 오류 신고 본문 수정
     */
    fun updateContent(content: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _content.value = content
        }
    }

    /**
     * 오류 신고 등록
     */
    fun createFeedback(title: String, content: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            withContext(Dispatchers.IO) {
                createFeedbackUseCase(
                    command = CreateFeedbackUseCase.Command(
                        title = title,
                        content = content,
                    )
                )
            }

            _uiState.value = UiState.CreateSuccess
        }
    }

    /**
     * 오류 신고 등록 다이얼로그 오픈
     */
    fun createFeedbackDialogOpen(title: String) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _title.value = title
            _content.value = ""
            _uiState.value = UiState.Create
        }
    }

    /**
     * 오류 신고 등록 다이얼로그 닫기
     */
    fun createFeedbackDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 오류 신고 등록 확인 다이얼로그 오픈
     */
    fun createFeedbackConfirmDialogOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.CreateConfirm
        }
    }

    /**
     * 오류 신고 등록 확인 다이얼로그 닫기
     */
    fun createFeedbackConfirmDialogClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Create
        }
    }

    /**
     * 오류 신고 등록 완료 다이얼로그 닫기
     */
    fun createFeedbackSuccessDialogClose() {
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
package com.monglife.mongs.presentation.viewmodel.pages.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class MainPagerViewModel @Inject constructor(
) : BaseViewModel() {

    companion object {
        val EMPTY_PAGER_BRIGHTNESS = arrayOf(0.4f, 0.0f, 0.4f, 0.4f)
        const val EMPTY_PAGER_STATE_INIT = 1
        val EMPTY_PAGER_STATE_SIZE = EMPTY_PAGER_BRIGHTNESS.size

        val NORMAL_PAGER_BRIGHTNESS = arrayOf(0.4f, 0.4f, 0.0f, 0.4f, 0.4f)
        const val NORMAL_PAGER_STATE_INIT = 2
        val NORMAL_PAGER_STATE_SIZE = NORMAL_PAGER_BRIGHTNESS.size
    }

    private val _emptyPagerEvent = MutableSharedFlow<Int>()
    val emptyPagerEvent = _emptyPagerEvent.asSharedFlow()

    private val _normalPagerEvent = MutableSharedFlow<Int>()
    val normalPagerEvent = _normalPagerEvent.asSharedFlow()

    private val _isPagerChange = MediatorLiveData(false)
    val isPagerChange: LiveData<Boolean> get() = _isPagerChange

    /**
     * 메인 페이지 스크롤 이벤트
     */
    fun pagerScroll(page: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            _emptyPagerEvent.emit(max(0, min(page, EMPTY_PAGER_STATE_SIZE)))
            _normalPagerEvent.emit(max(0, min(page, NORMAL_PAGER_STATE_SIZE)))
        }
    }

    /**
     * 페이지 스크롤 여부 업데이트
     */
    fun updateIsPagerChange(isPagerChange: Boolean) {
        _isPagerChange.postValue(isPagerChange)
    }

    /**
     * 화면 초기화
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            _emptyPagerEvent.emit(EMPTY_PAGER_STATE_INIT)
            _normalPagerEvent.emit(NORMAL_PAGER_STATE_INIT)
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {}
}
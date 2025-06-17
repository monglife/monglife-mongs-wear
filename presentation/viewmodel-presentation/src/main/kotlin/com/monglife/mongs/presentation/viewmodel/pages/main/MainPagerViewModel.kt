package com.monglife.mongs.presentation.viewmodel.pages.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    val emptyPager: LiveData<Int> get() = _emptyPager
    private val _emptyPager = MediatorLiveData(EMPTY_PAGER_STATE_INIT)

    val emptyPagerBrightness: LiveData<Float> get() = _emptyPagerBrightness
    private val _emptyPagerBrightness = MediatorLiveData(EMPTY_PAGER_BRIGHTNESS[EMPTY_PAGER_STATE_INIT])

    val normalPager: LiveData<Int> get() = _normalPager
    private val _normalPager = MediatorLiveData(NORMAL_PAGER_STATE_INIT)

    val normalPagerBrightness: LiveData<Float> get() = _normalPagerBrightness
    private val _normalPagerBrightness = MediatorLiveData(NORMAL_PAGER_BRIGHTNESS[NORMAL_PAGER_STATE_INIT])

    fun emptyPagerScroll(page: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            _emptyPagerBrightness.postValue(EMPTY_PAGER_BRIGHTNESS[page])
            _emptyPager.postValue(page)
        }
    }

    fun normalPagerScroll(page: Int) {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            _normalPagerBrightness.postValue(NORMAL_PAGER_BRIGHTNESS[page])
            _normalPager.postValue(page)
        }
    }

    /**
     * 화면 초기화
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            emptyPagerScroll(EMPTY_PAGER_STATE_INIT)
            normalPagerScroll(NORMAL_PAGER_STATE_INIT)
        }
    }
}
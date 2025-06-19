package com.monglife.mongs.presentation.view.component.common.pagenation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import com.monglife.mongs.presentation.view.assets.MongsNavy
import com.monglife.mongs.presentation.view.assets.MongsWhite

@Composable
internal fun PageIndicator(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
) {
    val pageIndicatorState: PageIndicatorState = object : PageIndicatorState {
        override val pageOffset: Float
            get() = pagerState.currentPageOffsetFraction
        override val selectedPage: Int
            get() = pagerState.currentPage
        override val pageCount: Int
            get() = pagerState.pageCount
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPageIndicator(
            pageIndicatorState = pageIndicatorState,
            selectedColor = MongsNavy,
            unselectedColor = MongsWhite,
            indicatorSize = 6.dp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
    }
}

@Composable
internal fun PageIndicator(
    modifier: Modifier = Modifier,
    pageIndicatorState: PageIndicatorState,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPageIndicator(
            pageIndicatorState = pageIndicatorState,
            selectedColor = MongsNavy,
            unselectedColor = MongsWhite,
            indicatorSize = 6.dp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
    }
}
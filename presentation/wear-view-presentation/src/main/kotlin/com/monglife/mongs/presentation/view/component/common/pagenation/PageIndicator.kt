package com.monglife.mongs.presentation.view.component.common.pagenation

import androidx.compose.foundation.layout.Box
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
    indicatorSize: Int = 6,
    spacing: Int = 4,
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
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPageIndicator(
            pageIndicatorState = pageIndicatorState,
            selectedColor = MongsNavy,
            unselectedColor = MongsWhite,
            indicatorSize = indicatorSize.dp,
            spacing  = spacing.dp,
        )
    }
}

@Composable
internal fun PageIndicator(
    modifier: Modifier = Modifier,
    pageIndicatorState: PageIndicatorState,
    indicatorSize: Int = 6,
    spacing: Int = 4,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPageIndicator(
            pageIndicatorState = pageIndicatorState,
            selectedColor = MongsNavy,
            unselectedColor = MongsWhite,
            indicatorSize = indicatorSize.dp,
            spacing = spacing.dp,
        )
    }
}
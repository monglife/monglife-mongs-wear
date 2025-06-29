package com.monglife.mongs.presentation.view.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.MapResourceCode
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.background.MainBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.pagenation.PageIndicator
import com.monglife.mongs.presentation.viewmodel.pages.main.MainPagerViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun MainView(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainPagerViewModel: MainPagerViewModel = hiltViewModel<MainPagerViewModel>(parentEntry)

    val uiState = mainViewModel.uiState.collectAsState()
    val mongVo = mainViewModel.mongVo.collectAsState()
    val backgroundMapCode = mainViewModel.backgroundMapCode.collectAsState()

    val emptyPagerState = rememberPagerState(MainPagerViewModel.EMPTY_PAGER_STATE_INIT, 0f) { MainPagerViewModel.EMPTY_PAGER_STATE_SIZE }
    val normalPagerState = rememberPagerState(MainPagerViewModel.NORMAL_PAGER_STATE_INIT, 0f) { MainPagerViewModel.NORMAL_PAGER_STATE_SIZE }

    Box {
        if (uiState.value.loadingBar) {
            DefaultBackground()
            LoadingBar()
        } else {
            mongVo.value?.let {
                MainBackground(
                    backgroundMapCode = backgroundMapCode.value,
                    pagerState = normalPagerState,
                    pagerBrightnesses = MainPagerViewModel.NORMAL_PAGER_BRIGHTNESS,
                )

                PageIndicator(
                    pagerState = normalPagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 5.dp)
                        .zIndex(1f),
                )

                NormalMainPager(
                    modifier = Modifier.zIndex(2f),
                    navController = navController,
                    pagerState = normalPagerState,
                )

                val isPageChanging = remember {
                    derivedStateOf {
                        val currentPage = normalPagerState.currentPage
                        val ratio = normalPagerState.currentPageOffsetFraction.coerceIn(-1f, 1f)
                        val nextPage = if (ratio < 0) {
                            currentPage - 1
                        } else if (ratio > 0) {
                            currentPage + 1
                        } else currentPage
                        currentPage != nextPage
                    }
                }

                LaunchedEffect(isPageChanging.value) {
                    mainPagerViewModel.updatePageInfo(
                        page = normalPagerState.currentPage,
                        isPagerChange = isPageChanging.value
                    )
                }

                LaunchedEffect(Unit) {
                    mainPagerViewModel.normalPagerEvent.collectLatest { page ->
                        snapshotFlow { emptyPagerState.isScrollInProgress }.first { !it }
                        normalPagerState.animateScrollToPage(page = page)
                    }
                }
            } ?: run {
                MainBackground(
                    backgroundMapCode = backgroundMapCode.value,
                    pagerState = emptyPagerState,
                    pagerBrightnesses = MainPagerViewModel.EMPTY_PAGER_BRIGHTNESS,
                )

                PageIndicator(
                    pagerState = emptyPagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 5.dp)
                        .zIndex(1f),
                )

                EmptyMainPager(
                    modifier = Modifier.zIndex(2f),
                    navController = navController,
                    pagerState = emptyPagerState,
                )

                val isPageChanging = remember {
                    derivedStateOf {
                        val currentPage = emptyPagerState.currentPage
                        val ratio = emptyPagerState.currentPageOffsetFraction.coerceIn(-1f, 1f)
                        val nextPage = if (ratio < 0) {
                            currentPage - 1
                        } else if (ratio > 0) {
                            currentPage + 1
                        } else currentPage
                        currentPage != nextPage
                    }
                }

                LaunchedEffect(isPageChanging.value) {
                    mainPagerViewModel.updatePageInfo(
                        page = emptyPagerState.currentPage,
                        isPagerChange = isPageChanging.value
                    )
                }

                LaunchedEffect(Unit) {
                    mainPagerViewModel.emptyPagerEvent.collectLatest { page ->
                        snapshotFlow { emptyPagerState.isScrollInProgress }.first { !it }
                        emptyPagerState.animateScrollToPage(page)
                    }
                }
            }
        }
    }
}

@Composable
private fun NormalMainPager(
    modifier: Modifier = Modifier,
    navController: NavController,
    pagerState: PagerState,
) {
    Box(modifier = modifier) {
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> StepContent(navController = navController)

                1 -> ConditionContent(navController = navController)

                2 -> SlotContent(navController = navController)

                3 -> InteractionContent(navController = navController)

                4 -> ConfigureContent(navController = navController)
            }
        }
    }
}

@Composable
private fun EmptyMainPager(
    modifier: Modifier = Modifier,
    navController: NavController,
    pagerState: PagerState,
) {
    Box(modifier = modifier) {
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> StepContent(navController = navController)

                1 -> SlotContent(navController = navController)

                2 -> InteractionContent(navController = navController)

                3 -> ConfigureContent(navController = navController)
            }
        }
    }
}

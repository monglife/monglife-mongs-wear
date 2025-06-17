package com.monglife.mongs.presentation.view.router

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.pages.main.MainView
import com.monglife.mongs.presentation.view.utils.AlwaysOnScreen
import com.monglife.mongs.presentation.viewmodel.pages.main.MainPagerViewModel

@Composable
fun Router(
    modifier: Modifier = Modifier,
    mainPagerViewModel: MainPagerViewModel = hiltViewModel(),
) {
    val navController = rememberSwipeDismissableNavController()
    val emptyPagerState = rememberPagerState(MainPagerViewModel.EMPTY_PAGER_STATE_INIT, 0f) { MainPagerViewModel.EMPTY_PAGER_STATE_SIZE }
    val pagerState = rememberPagerState(MainPagerViewModel.NORMAL_PAGER_STATE_INIT, 0f) { MainPagerViewModel.NORMAL_PAGER_STATE_SIZE }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = RouterPath.MainPager.route,
        modifier = modifier,
        route = "router"
    ) {
        /**
         * 메인 페이지
         */
        composable(route = RouterPath.MainPager.route) {
            AlwaysOnScreen {
                MainView(navController = navController)
            }
        }
    }
}
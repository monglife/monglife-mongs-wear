package com.monglife.mongs.presentation.view.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.background.MainBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.viewmodel.pages.main.MainPagerViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainViewModel

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun MainView(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainPagerViewModel: MainPagerViewModel = hiltViewModel<MainPagerViewModel>(parentEntry)

    val uiState = mainViewModel.uiState.collectAsState()
    val currentMongVo = mainViewModel.currentMongVo.collectAsState()
    val backgroundMapCode = mainViewModel.backgroundMapCode.collectAsState()

    val emptyPagerState = rememberPagerState(MainPagerViewModel.EMPTY_PAGER_STATE_INIT, 0f) { MainPagerViewModel.EMPTY_PAGER_STATE_SIZE }
    val normalPagerState = rememberPagerState(MainPagerViewModel.NORMAL_PAGER_STATE_INIT, 0f) { MainPagerViewModel.NORMAL_PAGER_STATE_SIZE }

    Box {
        if (uiState.value.loadingBar) {
            DefaultBackground()
            LoadingBar()
        } else {
            MainBackground(
                backgroundMapCode = backgroundMapCode.value,
                pagerState = normalPagerState,
                pagerBrightnesses = MainPagerViewModel.NORMAL_PAGER_BRIGHTNESS,
            )
        }
    }
}

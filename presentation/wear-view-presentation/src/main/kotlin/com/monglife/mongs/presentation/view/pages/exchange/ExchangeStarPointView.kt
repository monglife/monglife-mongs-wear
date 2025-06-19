package com.monglife.mongs.presentation.view.pages.exchange

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun ExchangeStepView(
    navController: NavController,
    // TODO: Add viewModel if need
) {
    // TODO: For control mainPageView pagerState
//    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
//    val mainPagerViewModel: MainPagerViewModel = hiltViewModel<MainPagerViewModel>(parentEntry)
//    val isPagerChange = mainPagerViewModel.isPagerChange.observeAsState(false)

    // TODO: For control mainSlotView mongVo
//    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
//    val mainSlotViewModel: MainSlotViewModel = hiltViewModel<MainSlotViewModel>(parentEntry)
//    val mongVo = mainSlotViewModel.mongVo.observeAsState()

    Box {
        // TODO: Add background method
//         DefaultBackground()
//
//        if (ViewModel.uiState.loadingBar) {
//             LoadingBar()
//        } else {
//            ExchangeStepContent()
//        }
    }

        // TODO: call viewModel initialize method
//    LaunchedEffect(Unit) {
//        ViewModel.initialize()
//    }
}

@Composable
private fun ExchangeStepContent(
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {

    }
}
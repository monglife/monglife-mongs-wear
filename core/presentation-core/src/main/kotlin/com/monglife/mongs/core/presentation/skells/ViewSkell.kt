/*

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
internal fun View(
    navController: NavController,
    someViewModel: SomeViewModel = hiltViewModel(),
) {
    val uiState = someViewModel.uiState.collectAsState()

    // TODO: For control mainPageView pagerState
//    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
//    val mainPagerViewModel: MainPagerViewModel = hiltViewModel<MainPagerViewModel>(parentEntry)
//    val isPagerChange = mainPagerViewModel.isPagerChange.observeAsState(false)

    // TODO: For control mainSlotView mongVo
//    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
//    val mainSlotViewModel: MainSlotViewModel = hiltViewModel<MainSlotViewModel>(parentEntry)
//    val mongVo = mainSlotViewModel.mongVo.observeAsState()

    Box {
         DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Content()
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        someViewModel.uiEvent.collect { event ->
            when (event) {
                is SomeViewModel.UiEvent.SomeEvent -> {
                    // TODO: event consume logic
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun Content(
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {

    }
}

*/
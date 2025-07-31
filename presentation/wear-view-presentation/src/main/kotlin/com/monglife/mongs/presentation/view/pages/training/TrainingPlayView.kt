package com.monglife.mongs.presentation.view.pages.training

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.viewmodel.pages.training.TrainingPlayViewModel

@Composable
internal fun TrainingPlayView(
    trainingCode: String?,
    navController: NavController,
    trainingPlayViewModel: TrainingPlayViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = trainingPlayViewModel.uiState.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else if (uiState.value.enteringLoadingBar) {
            LaunchedEffect(Unit) {
                trainingPlayViewModel.enter(trainingCode = trainingCode)
            }
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                if (uiState.value.runnerContent) {
                    TrainingRunnerContent(trainingCode = trainingCode, navController = navController)
                } else if (uiState.value.basketballContent) {
                    TrainingBasketballContent(trainingCode = trainingCode, navController = navController)
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        trainingPlayViewModel.uiEvent.collect { event ->
            when (event) {
                is TrainingPlayViewModel.UiEvent.NavMenu -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.TrainingMenu.route, inclusive = false)
                }

                else -> {}
            }
        }
    }
}
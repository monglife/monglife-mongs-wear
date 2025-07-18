package com.monglife.mongs.presentation.view.pages.training

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.viewmodel.pages.training.TrainingRunnerViewModel

@Composable
internal fun TrainingRunnerView(
    navController: NavController,
    trainingRunnerViewModel: TrainingRunnerViewModel = hiltViewModel(),
) {
    val uiState = trainingRunnerViewModel.uiState.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            TrainingRunnerContent(trainingRunnerViewModel = trainingRunnerViewModel)
        }
    }
}

@Composable
private fun TrainingRunnerContent(
    modifier: Modifier = Modifier,
    trainingRunnerViewModel: TrainingRunnerViewModel
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {

    }
}
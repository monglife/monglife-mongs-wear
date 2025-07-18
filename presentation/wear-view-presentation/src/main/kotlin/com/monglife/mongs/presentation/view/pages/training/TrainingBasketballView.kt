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
import com.monglife.mongs.presentation.viewmodel.pages.training.TrainingBasketballViewModel

@Composable
internal fun TrainingBasketballView(
    navController: NavController,
    trainingBasketballViewModel: TrainingBasketballViewModel = hiltViewModel(),
) {
    val uiState = trainingBasketballViewModel.uiState.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            TrainingBasketballContent(trainingBasketballViewModel = trainingBasketballViewModel)
        }
    }
}

@Composable
private fun TrainingBasketballContent(
    modifier: Modifier = Modifier,
    trainingBasketballViewModel: TrainingBasketballViewModel,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {

    }
}
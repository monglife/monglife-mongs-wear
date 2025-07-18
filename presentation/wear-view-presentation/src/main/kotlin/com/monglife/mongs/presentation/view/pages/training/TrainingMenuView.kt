package com.monglife.mongs.presentation.view.pages.training

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.viewmodel.pages.training.TrainingMenuViewModel

@Composable
internal fun TrainingMenuView(
    navController: NavController,
    trainingMenuViewModel: TrainingMenuViewModel = hiltViewModel(),
) {
    val uiState = trainingMenuViewModel.uiState.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                TrainingMenuContent(
                    navController = navController,
                    trainingMenuViewModel = trainingMenuViewModel
                )
            }
        }
    }
}

@Composable
private fun TrainingMenuContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    trainingMenuViewModel: TrainingMenuViewModel,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {

    }
}
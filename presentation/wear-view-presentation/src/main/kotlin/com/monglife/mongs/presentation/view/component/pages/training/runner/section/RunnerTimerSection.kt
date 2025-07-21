package com.monglife.mongs.presentation.view.component.pages.training.runner.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.monglife.mongs.presentation.view.assets.MongsDarkYellow
import com.monglife.mongs.presentation.view.component.common.bar.ProgressIndicator
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.TrainingBasketballViewModel
import com.monglife.mongs.presentation.viewmodel.pages.training.runner.TrainingRunnerViewModel

@Composable
fun RunnerTimerSection(
    modifier: Modifier = Modifier,
    trainingRunnerViewModel: TrainingRunnerViewModel,
) {
    val trainingTypeVo = trainingRunnerViewModel.trainingTypeVo.collectAsState()
    val runnerVo = trainingRunnerViewModel.runnerVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        trainingTypeVo.value?.let { trainingTypeVo ->
            runnerVo.value?.let { runnerVo ->
                ProgressIndicator(
                    progress = runnerVo.timeMillis / (trainingTypeVo.timeout.toFloat() * 1000L) * 100,
                    indicatorColor = MongsDarkYellow,
                )
            }
        }
    }
}
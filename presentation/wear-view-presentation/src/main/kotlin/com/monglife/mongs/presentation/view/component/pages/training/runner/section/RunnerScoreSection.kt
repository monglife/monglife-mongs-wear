package com.monglife.mongs.presentation.view.component.pages.training.runner.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.component.common.textbox.ScoreBox
import com.monglife.mongs.presentation.viewmodel.pages.training.runner.TrainingRunnerViewModel

@Composable
fun RunnerScoreSection(
    modifier: Modifier = Modifier,
    trainingRunnerViewModel: TrainingRunnerViewModel,
) {
    val runnerVo = trainingRunnerViewModel.runnerVo.collectAsState()
    val trainingTypeVo = trainingRunnerViewModel.trainingTypeVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 14.dp)
    ) {
        runnerVo.value?.let { runnerVo ->
            trainingTypeVo.value?.let { trainingTypeVo ->
                ScoreBox(
                    score = runnerVo.score,
                    maxScore = trainingTypeVo.score,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
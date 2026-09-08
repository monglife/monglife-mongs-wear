package com.monglife.mongs.presentation.view.component.pages.training.rockPaperScissors.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.monglife.mongs.presentation.view.assets.MongsDarkYellow
import com.monglife.mongs.presentation.view.component.common.bar.ProgressIndicator
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.TrainingRockPaperScissorsViewModel

@Composable
fun RockPaperScissorsTimerSection(
    modifier: Modifier = Modifier,
    trainingRockPaperScissorsViewModel: TrainingRockPaperScissorsViewModel,
) {
    val trainingTypeVo = trainingRockPaperScissorsViewModel.trainingTypeVo.collectAsStateWithLifecycle()
    val timeMillis = trainingRockPaperScissorsViewModel.timeMillis.collectAsStateWithLifecycle()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        trainingTypeVo.value?.let { trainingTypeVo ->
            ProgressIndicator(
                progress = { timeMillis.value / (trainingTypeVo.timeout.toFloat() * 1000L) * 100 },
                indicatorColor = MongsDarkYellow,
            )
        }
    }
}
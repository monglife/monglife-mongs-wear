package com.monglife.mongs.presentation.view.component.pages.training.rockPaperScissors.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.component.common.textbox.ScoreBox
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.TrainingRockPaperScissorsViewModel

@Composable
internal fun RockPaperScissorsScoreSection(
    modifier: Modifier = Modifier,
    trainingRockPaperScissorsViewModel: TrainingRockPaperScissorsViewModel,
) {
    val trainingTypeVo = trainingRockPaperScissorsViewModel.trainingTypeVo.collectAsState()
    val rockPaperScissorsVo = trainingRockPaperScissorsViewModel.rockPaperScissorsVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 14.dp)
    ) {
        rockPaperScissorsVo.value?.let {
            trainingTypeVo.value?.let { trainingTypeVo ->
                ScoreBox(
                    score = it.score,
                    maxScore = trainingTypeVo.score,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
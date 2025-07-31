package com.monglife.mongs.presentation.view.component.pages.training.basketball.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.monglife.mongs.presentation.view.assets.MongsDarkPurple
import com.monglife.mongs.presentation.view.component.common.bar.ProgressIndicator
import com.monglife.mongs.presentation.view.component.common.textbox.ScoreBox
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.TrainingBasketballViewModel

@Composable
fun BasketballScoreSection(
    modifier: Modifier = Modifier,
    trainingBasketballViewModel: TrainingBasketballViewModel,
) {
    val basketballVo = trainingBasketballViewModel.basketballVo.collectAsState()
    val trainingTypeVo = trainingBasketballViewModel.trainingTypeVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 14.dp)
    ) {
        trainingTypeVo.value?.let { trainingTypeVo ->
            basketballVo.value?.let { basketballVo ->
                ScoreBox(
                    score = basketballVo.score,
                    maxScore = trainingTypeVo.score,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
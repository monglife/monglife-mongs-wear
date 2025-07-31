package com.monglife.mongs.presentation.view.component.pages.training.basketball.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.monglife.mongs.presentation.view.assets.MongsDarkYellow
import com.monglife.mongs.presentation.view.component.common.bar.ProgressIndicator
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.TrainingBasketballViewModel

@Composable
fun BasketballTimerSection(
    modifier: Modifier = Modifier,
    trainingBasketballViewModel: TrainingBasketballViewModel,
) {
    val trainingTypeVo = trainingBasketballViewModel.trainingTypeVo.collectAsState()
    val basketballVo = trainingBasketballViewModel.basketballVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        trainingTypeVo.value?.let { trainingTypeVo ->
            basketballVo.value?.let { basketballVo ->
                ProgressIndicator(
                    progress = basketballVo.timeMillis / (trainingTypeVo.timeout.toFloat() * 1000L) * 100,
                    indicatorColor = MongsDarkYellow,
                )
            }
        }
    }
}
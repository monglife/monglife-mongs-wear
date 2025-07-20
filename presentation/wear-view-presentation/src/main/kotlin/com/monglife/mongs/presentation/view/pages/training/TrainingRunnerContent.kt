package com.monglife.mongs.presentation.view.pages.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.monglife.mongs.presentation.view.component.pages.training.Hurdle
import com.monglife.mongs.presentation.view.component.pages.training.TrainingPlayer
import com.monglife.mongs.presentation.viewmodel.pages.training.runner.TrainingRunnerViewModel
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun TrainingRunnerContent(
    modifier: Modifier = Modifier,
    trainingRunnerViewModel: TrainingRunnerViewModel = hiltViewModel(),
) {
    val currentMongVo = trainingRunnerViewModel.currentMongVo.collectAsState()
    val trainingPlayerVo = trainingRunnerViewModel.trainingPlayerVo.collectAsState()
    val hurdleVos = trainingRunnerViewModel.hurdleVos.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { trainingRunnerViewModel.trainingPlayerJump() },
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.72f)
            ) {
                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.zIndex(1f)
                    ) {
                        hurdleVos.value.forEachIndexed { index, hurdle ->
                            Hurdle(
                                image = R.drawable.icon_poop,
                                height = hurdle.height,
                                width = hurdle.width,
                                modifier = Modifier
                                    .zIndex(-index.toFloat())
                                    .offset {
                                        IntOffset(
                                            y = (hurdle.py).dp.roundToPx(),
                                            x = (hurdle.px).dp.roundToPx(),
                                        )
                                    }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.zIndex(2f)
                    ) {
                        currentMongVo.value?.let {
                            trainingPlayerVo.value?.let { trainingPlayerVo ->
                                TrainingPlayer(
                                    mongCode = it.mongCode,
                                    height = trainingPlayerVo.height,
                                    width = trainingPlayerVo.width,
                                    modifier = Modifier
                                        .zIndex(1f)
                                        .offset {
                                            IntOffset(
                                                y = (trainingPlayerVo.py).dp.roundToPx(),
                                                x = (trainingPlayerVo.px).dp.roundToPx(),
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.28f)
            ) {
                Spacer(modifier = Modifier)
            }
        }
    }
}
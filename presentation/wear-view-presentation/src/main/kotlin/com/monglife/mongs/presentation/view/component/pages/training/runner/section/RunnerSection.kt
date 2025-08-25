package com.monglife.mongs.presentation.view.component.pages.training.runner.section

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
import com.monglife.mongs.presentation.view.component.pages.training.runner.Hurdle
import com.monglife.mongs.presentation.view.component.pages.training.runner.Player
import com.monglife.mongs.presentation.viewmodel.pages.training.runner.TrainingRunnerViewModel
import com.mongs.presentation.view.wear.R

@Composable
fun RunnerSection(
    modifier: Modifier = Modifier,
    trainingRunnerViewModel: TrainingRunnerViewModel
) {
    val currentMongVo = trainingRunnerViewModel.currentMongVo.collectAsState()
    val runnerVo = trainingRunnerViewModel.runnerVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
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
                runnerVo.value?.let { runnerVo ->
                    Box(
                        contentAlignment = Alignment.BottomStart,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier.zIndex(1f)
                        ) {
                            runnerVo.runnerHurdleVos.forEachIndexed { index, runnerHurdleVo ->
                                Hurdle(
                                    image = R.drawable.icon_poop,
                                    height = runnerHurdleVo.height,
                                    width = runnerHurdleVo.width,
                                    modifier = Modifier
                                        .zIndex(-index.toFloat())
                                        .offset {
                                            IntOffset(
                                                y = (runnerHurdleVo.py).dp.roundToPx(),
                                                x = (runnerHurdleVo.px).dp.roundToPx(),
                                            )
                                        }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.zIndex(2f)
                        ) {
                            currentMongVo.value?.let {
                                runnerVo.runnerPlayerVo.let { runnerPlayerVo ->
                                    Player(
                                        mongCode = it.mongCode,
                                        height = runnerPlayerVo.height,
                                        width = runnerPlayerVo.width,
                                        modifier = Modifier
                                            .zIndex(1f)
                                            .offset {
                                                IntOffset(
                                                    y = (runnerPlayerVo.py).dp.roundToPx(),
                                                    x = (runnerPlayerVo.px).dp.roundToPx(),
                                                )
                                            }
                                    )
                                }
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
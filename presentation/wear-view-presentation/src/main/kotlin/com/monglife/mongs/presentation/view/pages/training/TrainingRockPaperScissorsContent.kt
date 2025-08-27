package com.monglife.mongs.presentation.view.pages.training

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.TrainingBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.pages.training.rockPaperScissors.section.RockPaperScissorsScoreSection
import com.monglife.mongs.presentation.view.component.pages.training.rockPaperScissors.section.RockPaperScissorsSection
import com.monglife.mongs.presentation.view.component.pages.training.rockPaperScissors.section.RockPaperScissorsTimerSection
import com.monglife.mongs.presentation.view.dialog.pages.training.TrainingEnteringDialog
import com.monglife.mongs.presentation.view.dialog.pages.training.TrainingOverDialog
import com.monglife.mongs.presentation.view.dialog.pages.training.rockPaperScissors.RockPaperScissorsPickDialog
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.TrainingRockPaperScissorsViewModel
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.enums.RockPaperScissorsPickCode

@Composable
internal fun TrainingRockPaperScissorsContent(
    trainingCode: String?,
    navController: NavController,
    trainingRockPaperScissorsViewModel: TrainingRockPaperScissorsViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = trainingRockPaperScissorsViewModel.uiState.collectAsState()
    val currentMongVo = trainingRockPaperScissorsViewModel.currentMongVo.collectAsState()
    val trainingTypeVo = trainingRockPaperScissorsViewModel.trainingTypeVo.collectAsState()
    val trainingEndVo = trainingRockPaperScissorsViewModel.trainingEndVo.collectAsState()
    val rockPaperScissorsVo = trainingRockPaperScissorsViewModel.rockPaperScissorsVo.collectAsState()
    val isStart = trainingRockPaperScissorsViewModel.isStart.collectAsState()
    val isProcess = trainingRockPaperScissorsViewModel.isProcess.collectAsState()
    val timeMillis = trainingRockPaperScissorsViewModel.timeMillis.collectAsState()

    Box {
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(0f)) {
                TrainingBackground()
            }

            Box(modifier = Modifier.zIndex(1f)) {
                if (uiState.value.playSection) {
                    RockPaperScissorsScoreSection(
                        trainingRockPaperScissorsViewModel = trainingRockPaperScissorsViewModel,
                    )

                    RockPaperScissorsTimerSection(
                        trainingRockPaperScissorsViewModel = trainingRockPaperScissorsViewModel,
                    )
                }
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.playSection) {
                    RockPaperScissorsSection(
                        trainingRockPaperScissorsViewModel = trainingRockPaperScissorsViewModel,
                    )
                } else if (uiState.value.pickEndLoadingBar) {
                    LoadingBar()
                }
            }

            Box(modifier = Modifier.zIndex(3f)) {
                trainingTypeVo.value?.let { trainingTypeVo ->
                    if (uiState.value.enteringDialog) {
                        TrainingEnteringDialog(
                            trainingTypeVo = trainingTypeVo,
                            onClick = trainingRockPaperScissorsViewModel::start,
                        )
                    } else if (uiState.value.endDialog) {
                        trainingEndVo.value?.let { trainingEndVo ->
                            TrainingOverDialog(
                                isSuccess = trainingEndVo.isSuccess,
                                rewardPayPoint = trainingEndVo.rewardPayPoint,
                                onTrainingEndClick = trainingRockPaperScissorsViewModel::exit,
                            )
                        }
                    } else if (uiState.value.pickDialog) {
                        RockPaperScissorsPickDialog(
                            maxSeconds = 5,
                            onRockClick = { trainingRockPaperScissorsViewModel.pick(pickCode = RockPaperScissorsPickCode.ROCK) },
                            onPaperClick = { trainingRockPaperScissorsViewModel.pick(pickCode = RockPaperScissorsPickCode.PAPER) },
                            onScissorsClick = { trainingRockPaperScissorsViewModel.pick(pickCode = RockPaperScissorsPickCode.SCISSORS) },
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(isProcess.value) {
        rockPaperScissorsVo.value?.let {
            trainingTypeVo.value?.let { trainingTypeVo ->
                currentMongVo.value?.let { currentMongVo ->
                    if (isStart.value && !isProcess.value) {
                        trainingRockPaperScissorsViewModel.end(
                            mongId = currentMongVo.mongId,
                            trainingCode = trainingTypeVo.trainingCode,
                            score = it.score,
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(rockPaperScissorsVo.value, timeMillis.value) {
        rockPaperScissorsVo.value?.let {
            trainingTypeVo.value?.let { trainingTypeVo ->
                if (isStart.value) {
                    if (it.score >= trainingTypeVo.score || timeMillis.value >= trainingTypeVo.timeout * 1000L) {
                        trainingRockPaperScissorsViewModel.stop()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        trainingRockPaperScissorsViewModel.enter(trainingCode = trainingCode)
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        trainingRockPaperScissorsViewModel.uiEvent.collect { event ->
            when (event) {
                is TrainingRockPaperScissorsViewModel.UiEvent.NavMenu -> {
                    if (event.message.isNotBlank()) {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                    navController.popBackStack(RouterPath.TrainingMenu.route, inclusive = false)
                }

                else -> {}
            }
        }
    }
}
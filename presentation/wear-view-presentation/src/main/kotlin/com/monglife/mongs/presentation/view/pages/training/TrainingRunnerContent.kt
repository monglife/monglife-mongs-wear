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
import com.monglife.mongs.presentation.view.component.pages.training.runner.section.RunnerScoreSection
import com.monglife.mongs.presentation.view.component.pages.training.runner.section.RunnerSection
import com.monglife.mongs.presentation.view.dialog.pages.training.TrainingEnteringDialog
import com.monglife.mongs.presentation.view.dialog.pages.training.TrainingOverDialog
import com.monglife.mongs.presentation.viewmodel.pages.training.runner.TrainingRunnerViewModel

@Composable
internal fun TrainingRunnerContent(
    trainingCode: String?,
    navController: NavController,
    trainingRunnerViewModel: TrainingRunnerViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = trainingRunnerViewModel.uiState.collectAsState()
    val currentMongVo = trainingRunnerViewModel.currentMongVo.collectAsState()
    val runnerVo = trainingRunnerViewModel.runnerVo.collectAsState()
    val trainingTypeVo = trainingRunnerViewModel.trainingTypeVo.collectAsState()
    val trainingEndVo = trainingRunnerViewModel.trainingEndVo.collectAsState()

    Box {
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            runnerVo.value?.let {
                Box(modifier = Modifier.zIndex(0f)) {
                    TrainingBackground(isMoving = it.isStart && it.isProcess)
                }

                Box(modifier = Modifier.zIndex(1f)) {
                    if (uiState.value.playSection) {
                        RunnerScoreSection(
                            modifier = Modifier.zIndex(1f),
                            trainingRunnerViewModel = trainingRunnerViewModel
                        )
                    }
                }

                Box(modifier = Modifier.zIndex(2f)) {
                    if (uiState.value.playSection) {
                        RunnerSection(trainingRunnerViewModel = trainingRunnerViewModel)
                    }
                }

                Box(modifier = Modifier.zIndex(3f)) {
                    trainingTypeVo.value?.let { trainingTypeVo ->
                        if (uiState.value.enteringDialog) {
                            TrainingEnteringDialog(
                                trainingTypeVo = trainingTypeVo,
                                onClick = trainingRunnerViewModel::start
                            )
                        } else if (uiState.value.endDialog) {
                            trainingEndVo.value?.let { trainingEndVo ->
                                TrainingOverDialog(
                                    isSuccess = trainingEndVo.isSuccess,
                                    rewardPayPoint = trainingEndVo.rewardPayPoint,
                                    onTrainingEndClick = trainingRunnerViewModel::exit,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(runnerVo.value?.isProcess) {
        runnerVo.value?.let {
            trainingTypeVo.value?.let { trainingTypeVo ->
                currentMongVo.value?.let { currentMongVo ->
                    if (it.isStart && !it.isProcess) {
                        trainingRunnerViewModel.end(
                            mongId = currentMongVo.mongId,
                            trainingCode = trainingTypeVo.trainingCode,
                            score = it.score,
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(runnerVo.value) {
        runnerVo.value?.let {
            trainingTypeVo.value?.let { trainingTypeVo ->
                if (it.isStart) {
                    if (it.score >= trainingTypeVo.score) {
                        trainingRunnerViewModel.stop()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        trainingRunnerViewModel.enter(trainingCode = trainingCode)
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        trainingRunnerViewModel.uiEvent.collect { event ->
            when (event) {
                is TrainingRunnerViewModel.UiEvent.NavMenu -> {
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
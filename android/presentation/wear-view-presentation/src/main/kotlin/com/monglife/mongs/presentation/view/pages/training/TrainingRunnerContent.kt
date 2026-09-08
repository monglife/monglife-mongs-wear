package com.monglife.mongs.presentation.view.pages.training

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
internal fun TrainingRunnerContent(
    trainingCode: String?,
    navController: NavController,
    trainingRunnerViewModel: TrainingRunnerViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = trainingRunnerViewModel.uiState.collectAsStateWithLifecycle()
    val currentMongVo = trainingRunnerViewModel.currentMongVo.collectAsStateWithLifecycle()
    val runnerVo = trainingRunnerViewModel.runnerVo.collectAsStateWithLifecycle()
    val trainingTypeVo = trainingRunnerViewModel.trainingTypeVo.collectAsStateWithLifecycle()
    val trainingEndVo = trainingRunnerViewModel.trainingEndVo.collectAsStateWithLifecycle()

    Box {
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(0f)) {
                TrainingBackground(
                    isMoving = runnerVo.value?.let {
                        it.isStart && it.isProcess
                    } ?: false
                )
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
                if (uiState.value.enteringDialog) {
                    trainingTypeVo.value?.let { trainingTypeVo ->
                        TrainingEnteringDialog(
                            trainingTypeVo = trainingTypeVo,
                            onClick = trainingRunnerViewModel::start
                        )
                    }
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

    /**
     * 목표 점수 도달 감시
     *
     * 이전에는 LaunchedEffect(runnerVo.value) 였는데, 엔진이 16ms 마다 새 VO 를 emit 하므로
     * 초당 62.5회 코루틴이 취소·재시작됐다. snapshotFlow 로 조건만 관찰하면
     * 컴포지션을 건드리지 않고 조건이 바뀔 때만 반응한다.
     */
    LaunchedEffect(Unit) {
        snapshotFlow {
            val vo = runnerVo.value
            val trainingType = trainingTypeVo.value

            vo != null && trainingType != null && vo.isStart && vo.score >= trainingType.score
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { trainingRunnerViewModel.stop() }
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

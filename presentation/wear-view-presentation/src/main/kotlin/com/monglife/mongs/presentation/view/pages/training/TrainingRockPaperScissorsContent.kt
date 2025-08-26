package com.monglife.mongs.presentation.view.pages.training

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.TrainingBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.dialog.pages.training.TrainingEnteringDialog
import com.monglife.mongs.presentation.view.dialog.pages.training.TrainingOverDialog
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPagerScissors.TrainingRockPagerScissorsViewModel

@Composable
internal fun TrainingRockPaperScissorsContent(
    trainingCode: String?,
    navController: NavController,
    trainingRockPagerScissorsViewModel: TrainingRockPagerScissorsViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
    windowInfo: WindowInfo = LocalWindowInfo.current,
) {
    val uiState = trainingRockPagerScissorsViewModel.uiState.collectAsState()
    val currentMongVo = trainingRockPagerScissorsViewModel.currentMongVo.collectAsState()
    val trainingTypeVo = trainingRockPagerScissorsViewModel.trainingTypeVo.collectAsState()
    val trainingEndVo = trainingRockPagerScissorsViewModel.trainingEndVo.collectAsState()

    Box {
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(0f)) {
                TrainingBackground()
            }

            Box(modifier = Modifier.zIndex(1f)) {
                if (uiState.value.playSection) {
                    // TODO: 플레이 부가 섹션
                }
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.playSection) {
                    // TODO: 플레이 섹션
                }
            }

            Box(modifier = Modifier.zIndex(3f)) {
                trainingTypeVo.value?.let { trainingTypeVo ->
                    if (uiState.value.enteringDialog) {
                        TrainingEnteringDialog(
                            trainingTypeVo = trainingTypeVo,
                            onClick = trainingRockPagerScissorsViewModel::start,
                        )
                    } else if (uiState.value.endDialog) {
                        trainingEndVo.value?.let { trainingEndVo ->
                            TrainingOverDialog(
                                isSuccess = trainingEndVo.isSuccess,
                                rewardPayPoint = trainingEndVo.rewardPayPoint,
                                onTrainingEndClick = trainingRockPagerScissorsViewModel::exit,
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        trainingRockPagerScissorsViewModel.enter(trainingCode = trainingCode)
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        trainingRockPagerScissorsViewModel.uiEvent.collect { event ->
            when (event) {
                is TrainingRockPagerScissorsViewModel.UiEvent.NavMenu -> {
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
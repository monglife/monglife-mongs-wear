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
import com.monglife.mongs.presentation.viewmodel.pages.training.cham.TrainingChamViewModel

@Composable
internal fun TrainingChamContent(
    trainingCode: String?,
    navController: NavController,
    trainingChamViewModel: TrainingChamViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
    windowInfo: WindowInfo = LocalWindowInfo.current,
) {
    val uiState = trainingChamViewModel.uiState.collectAsState()
    val currentMongVo = trainingChamViewModel.currentMongVo.collectAsState()
    val trainingTypeVo = trainingChamViewModel.trainingTypeVo.collectAsState()
    val trainingEndVo = trainingChamViewModel.trainingEndVo.collectAsState()

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
                            onClick = trainingChamViewModel::start,
                        )
                    } else if (uiState.value.endDialog) {
                        trainingEndVo.value?.let { trainingEndVo ->
                            TrainingOverDialog(
                                isSuccess = trainingEndVo.isSuccess,
                                rewardPayPoint = trainingEndVo.rewardPayPoint,
                                onTrainingEndClick = trainingChamViewModel::exit,
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        trainingChamViewModel.enter(trainingCode = trainingCode)
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        trainingChamViewModel.uiEvent.collect { event ->
            when (event) {
                is TrainingChamViewModel.UiEvent.NavMenu -> {
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
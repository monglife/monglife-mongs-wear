package com.monglife.mongs.presentation.view.pages.training

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.TrainingBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.pages.training.basketball.section.BasketballScoreSection
import com.monglife.mongs.presentation.view.component.pages.training.basketball.section.BasketballSection
import com.monglife.mongs.presentation.view.component.pages.training.basketball.section.BasketballTimerSection
import com.monglife.mongs.presentation.view.dialog.pages.training.TrainingEnteringDialog
import com.monglife.mongs.presentation.view.dialog.pages.training.TrainingOverDialog
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.TrainingBasketballViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
internal fun TrainingBasketballContent(
    trainingCode: String?,
    navController: NavController,
    trainingBasketballViewModel: TrainingBasketballViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
    configuration: Configuration = LocalConfiguration.current,
) {
    val uiState = trainingBasketballViewModel.uiState.collectAsState()
    val currentMongVo = trainingBasketballViewModel.currentMongVo.collectAsState()
    val basketballVo = trainingBasketballViewModel.basketballVo.collectAsState()
    val trainingTypeVo = trainingBasketballViewModel.trainingTypeVo.collectAsState()
    val trainingEndVo = trainingBasketballViewModel.trainingEndVo.collectAsState()

    Box {
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            basketballVo.value?.let {
                Box(modifier = Modifier.zIndex(0f)) {
                    TrainingBackground()
                }

                Box(modifier = Modifier.zIndex(1f)) {
                    if (uiState.value.playSection) {
                        BasketballScoreSection(
                            modifier = Modifier.zIndex(1f),
                            trainingBasketballViewModel = trainingBasketballViewModel
                        )

                        BasketballTimerSection(
                            modifier = Modifier.zIndex(2f),
                            trainingBasketballViewModel = trainingBasketballViewModel
                        )

                        trainingTypeVo.value?.let { trainingTypeVo ->
                            currentMongVo.value?.let { currentMongVo ->
                                LaunchedEffect(it.score, it.timeMillis) {
                                    if (it.score >= trainingTypeVo.score || it.timeMillis >= trainingTypeVo.timeout * 1000L) {
                                        trainingBasketballViewModel.stop()
                                    }
                                }

                                LaunchedEffect(it.isProcess, uiState.value) {
                                    if (!it.isProcess && uiState.value.stopSection) {
                                        trainingBasketballViewModel.end(
                                            mongId = currentMongVo.mongId,
                                            trainingCode = trainingTypeVo.trainingCode,
                                            score = it.score,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.zIndex(2f)) {
                    if (uiState.value.playSection) {
                        BasketballSection(trainingBasketballViewModel = trainingBasketballViewModel)
                    }
                }

                Box(modifier = Modifier.zIndex(3f)) {
                    trainingTypeVo.value?.let { trainingTypeVo ->
                        if (uiState.value.enteringDialog) {
                            TrainingEnteringDialog(
                                trainingTypeVo = trainingTypeVo,
                                onClick = trainingBasketballViewModel::start
                            )
                        } else if (uiState.value.endDialog) {
                            trainingEndVo.value?.let { trainingEndVo ->
                                TrainingOverDialog(
                                    isSuccess = trainingEndVo.isSuccess,
                                    rewardPayPoint = trainingEndVo.rewardPayPoint,
                                    onTrainingEndClick = trainingBasketballViewModel::exit,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        trainingBasketballViewModel.enter(
            trainingCode = trainingCode,
            ballInitY = configuration.screenHeightDp.toFloat() * 1.73f,
            ballInitX = configuration.screenWidthDp.toFloat(),
            basketInitY = configuration.screenHeightDp.toFloat() * 0.73f,
            basketInitX = configuration.screenWidthDp.toFloat(),
        )
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        trainingBasketballViewModel.uiEvent.collect { event ->
            when (event) {
                is TrainingBasketballViewModel.UiEvent.NavMenu -> {
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
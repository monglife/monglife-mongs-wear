package com.monglife.mongs.presentation.view.pages.training

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import com.monglife.mongs.presentation.view.assets.TrainingResourceCode
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.chip.IconChip
import com.monglife.mongs.presentation.viewmodel.pages.training.TrainingMenuViewModel

@Composable
internal fun TrainingMenuView(
    navController: NavController,
    trainingMenuViewModel: TrainingMenuViewModel = hiltViewModel(),
) {
    val uiState = trainingMenuViewModel.uiState.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                TrainingMenuContent(
                    navController = navController,
                    trainingMenuViewModel = trainingMenuViewModel
                )
            }
        }
    }
}

@Composable
private fun TrainingMenuContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    trainingMenuViewModel: TrainingMenuViewModel,
) {
    val trainingTypeVos = trainingMenuViewModel.trainingTypeVos.collectAsState()

    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        PositionIndicator(scalingLazyListState = listState)

        ScalingLazyColumn(
            contentPadding = PaddingValues(vertical = 60.dp, horizontal = 6.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState,
            autoCentering = null,
        ) {
            for (trainingTypeVo in trainingTypeVos.value) {
                item {
                    TrainingResourceCode.getResource(code = trainingTypeVo.trainingCode).let {
                        IconChip(
                            icon = it.iconCode,
                            border = it.borderCode,
                            fontColor = Color.White,
                            backgroundColor = Color.Black,
                            label = trainingTypeVo.trainingName,
                            onClick = { navController.navigate(it.routerPath) },
                        )
                    }
                }
            }
        }
    }
}
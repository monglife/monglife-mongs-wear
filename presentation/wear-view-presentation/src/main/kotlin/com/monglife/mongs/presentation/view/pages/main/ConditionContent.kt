package com.monglife.mongs.presentation.view.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.MongsBlue
import com.monglife.mongs.presentation.view.assets.MongsGreen
import com.monglife.mongs.presentation.view.assets.MongsPink
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.pages.main.condition.ConditionSection
import com.monglife.mongs.presentation.viewmodel.pages.main.MainConditionViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainPagerViewModel
import com.monglife.mongs.presentation.wear.component.common.bar.ProgressIndicator
import com.mongs.wear.presentation.view.wear.R

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun ConditionContent(
    navController: NavController,
    mainConditionViewModel: MainConditionViewModel = hiltViewModel(),
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainPagerViewModel: MainPagerViewModel = hiltViewModel<MainPagerViewModel>(parentEntry)
    val isPagerChange = mainPagerViewModel.isPagerChange.collectAsState()

    val uiState = mainConditionViewModel.uiState.collectAsState()
    val currentMongVo = mainConditionViewModel.currentMongVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            currentMongVo.value?.let {
                if (!isPagerChange.value) {
                    ProgressIndicator(
                        modifier = Modifier.zIndex(1f),
                        progress = it.expRatio.toFloat(),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .zIndex(2f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ConditionSection(
                            icon = R.drawable.icon_healthy,
                            progress = it.healthyRatio.toFloat(),
                            indicatorColor = MongsPink
                        )
                        ConditionSection(
                            icon = R.drawable.icon_satiety,
                            progress = it.satietyRatio.toFloat(),
                            indicatorColor = MongsYellow
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ConditionSection(
                            icon = R.drawable.icon_strength,
                            progress = it.strengthRatio.toFloat(),
                            indicatorColor = MongsGreen
                        )
                        ConditionSection(
                            icon = R.drawable.icon_fatigue,
                            progress = it.fatigueRatio.toFloat(),
                            indicatorColor = MongsBlue
                        )
                    }
                }
            }
        }
    }
}

package com.monglife.mongs.presentation.view.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.textbox.PayPoint
import com.monglife.mongs.presentation.view.dialog.common.PermissionDialog
import com.monglife.mongs.presentation.viewmodel.pages.main.MainPagerViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainStepViewModel

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun StepContent(
    navController: NavController,
    mainStepViewModel: MainStepViewModel = hiltViewModel(),
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainPagerViewModel: MainPagerViewModel = hiltViewModel<MainPagerViewModel>(parentEntry)
    val isPagerChange = mainPagerViewModel.isPagerChange.collectAsState()

    val uiState = mainStepViewModel.uiState.collectAsState()
    val currentMongVo = mainStepViewModel.currentMongVo.collectAsState()
    val currentWalkingCount = mainStepViewModel.currentWalkingCount.collectAsState()
    val activityPermission = mainStepViewModel.activityPermission.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .zIndex(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f),
                ) {
                    currentMongVo.value?.let {
                        PayPoint(payPoint = currentMongVo.value?.payPoint ?: 0)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f),
                ) {
                    Text(
                        text = if (currentWalkingCount.value < 0) "-" else "${currentWalkingCount.value} 걸음",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 18.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                ) {
                    currentMongVo.value?.let {
                        if (activityPermission.value) {
                            BlueButton(
                                text = "환전",
                                width = 70,
                                onClick = { navController.navigate(RouterPath.ExchangeStep.route) },
                                disable = currentMongVo.value?.let {
                                    it.stateCode in arrayOf(
                                        MongStateCode.DEAD,
                                        MongStateCode.DELETE
                                    )
                                } ?: false,
                            )
                        }
                    }
                }
            }

            if (!activityPermission.value && !isPagerChange.value) {
                PermissionDialog(
                    modifier = Modifier.zIndex(2f),
                    permissionName = "활동",
                    callback = mainStepViewModel::verifyActivityPermission,
                )
            }
        }
    }
}
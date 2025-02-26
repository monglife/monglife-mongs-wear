package com.mongs.wear.presentation.pages.main.walking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
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
import com.mongs.wear.core.enums.MongStateCode
import com.mongs.wear.domain.management.vo.MongVo
import com.mongs.wear.presentation.assets.DAL_MU_RI
import com.mongs.wear.presentation.assets.MongsWhite
import com.mongs.wear.presentation.assets.NavItem
import com.mongs.wear.presentation.component.common.bar.LoadingBar
import com.mongs.wear.presentation.component.common.button.BlueButton
import com.mongs.wear.presentation.component.common.textbox.PayPoint
import com.mongs.wear.presentation.dialog.common.ConfirmAndCancelDialog
import com.mongs.wear.presentation.dialog.common.ConfirmDialog
import com.mongs.wear.presentation.dialog.error.NeedPermissionDialog


@Composable
fun MainWalkingView(
    navController: NavController,
    mainWalkingViewModel: MainWalkingViewModel = hiltViewModel(),
) {
    val mongVo = mainWalkingViewModel.mongVo.observeAsState()
    val steps = mainWalkingViewModel.steps.observeAsState(0)
    val payPoint = mainWalkingViewModel.payPoint.observeAsState(0)
    val activityPermission = mainWalkingViewModel.activityPermission.observeAsState(true)

    Box {
        if (mainWalkingViewModel.uiState.loadingBar) {
            MainWalkingLoadingBar()
        } else {
            MainWalkingContent(
                mongVo = mongVo.value,
                steps = steps.value,
                payPoint = payPoint.value,
                resetSteps = {
                    mainWalkingViewModel.uiState.resetStepsDialog = true
                },
                navExchangeWalking = {
                    navController.navigate(NavItem.ExchangeWalking.route)
                },
                modifier = Modifier.zIndex(1f),
            )

            if (!activityPermission.value) {
                NeedPermissionDialog(
                    permissionName = "활동",
                    permissionSettingEndEvent = mainWalkingViewModel::refreshActivityPermission,
                    modifier = Modifier.zIndex(2f)
                )
            } else if (mainWalkingViewModel.uiState.resetStepsDialog) {
                ConfirmAndCancelDialog(
                    text = "현재까지의\n걸음 수가 초기화됩니다\n진행하시겠습니까?",
                    cancel = {
                        mainWalkingViewModel.uiState.resetStepsDialog = false
                    },
                    confirm = mainWalkingViewModel::resetSteps,
                    modifier = Modifier.zIndex(2f)
                )
            }
        }
    }
}

@Composable
private fun MainWalkingContent(
    mongVo: MongVo?,
    steps: Int,
    payPoint: Int,
    resetSteps: () -> Unit,
    navExchangeWalking: () -> Unit,
    modifier: Modifier = Modifier.zIndex(0f),
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                PayPoint(payPoint = payPoint)
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                Text(
                    text = "$steps 걸음",
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
//                BlueButton(
//                    text = "초기화",
//                    width = 70,
//                    onClick = resetSteps,
//                )
//                Spacer(modifier = Modifier.width(8.dp))
                BlueButton(
                    text = "환전",
                    width = 70,
                    disable = mongVo?.let {
                        mongVo.stateCode in arrayOf(MongStateCode.DEAD, MongStateCode.DELETE)
                    } ?: true,
                    onClick = navExchangeWalking,
                )


            }
        }
    }
}

@Composable
private fun MainWalkingLoadingBar(
    modifier: Modifier = Modifier.zIndex(0f),
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        LoadingBar()
    }
}
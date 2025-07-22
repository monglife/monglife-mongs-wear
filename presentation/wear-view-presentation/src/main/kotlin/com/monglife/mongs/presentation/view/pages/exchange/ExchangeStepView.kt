package com.monglife.mongs.presentation.view.pages.exchange

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.button.SelectButton
import com.monglife.mongs.presentation.view.component.common.textbox.PayPointBox
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.dialog.common.PermissionDialog
import com.monglife.mongs.presentation.viewmodel.pages.exchange.ExchangeStepViewModel
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun ExchangeStepView(
    navController: NavController,
    exchangeStepViewModel: ExchangeStepViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = exchangeStepViewModel.uiState.collectAsState()
    val currentMongVo = exchangeStepViewModel.currentMongVo.collectAsState()
    val exchangeCount = exchangeStepViewModel.exchangeCount.collectAsState()
    val chargePayPoint = exchangeStepViewModel.chargePayPoint.collectAsState()
    val activityPermission = exchangeStepViewModel.activityPermission.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                ExchangeStepContent(exchangeStepViewModel = exchangeStepViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (!activityPermission.value) {
                    PermissionDialog(
                        permissionName = "활동",
                        callback = exchangeStepViewModel::verifyActivityPermission,
                    )
                } else if (uiState.value.confirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "$${chargePayPoint.value}\n환전하시겠습니까?",
                        cancel = exchangeStepViewModel::exchangeConfirmDialogClose,
                        confirm = {
                            currentMongVo.value?.let {
                                exchangeStepViewModel.exchange(
                                    mongId = it.mongId,
                                    exchangeCount = exchangeCount.value
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        exchangeStepViewModel.uiEvent.collect { event ->
            when (event) {
                is ExchangeStepViewModel.UiEvent.NavMenu -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.ExchangeMenu.route, inclusive = false)
                }
                is ExchangeStepViewModel.UiEvent.Exchange -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ExchangeStepContent(
    modifier: Modifier = Modifier,
    exchangeStepViewModel: ExchangeStepViewModel,
) {
    val currentMongVo = exchangeStepViewModel.currentMongVo.collectAsState()
    val walkingCount = exchangeStepViewModel.walkingCount.collectAsState()
    val exchangeCount = exchangeStepViewModel.exchangeCount.collectAsState()
    val chargePayPoint = exchangeStepViewModel.chargePayPoint.collectAsState()

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                currentMongVo.value?.let {
                    PayPointBox(payPoint = it.payPoint)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                SelectButton(
                    leftBtnDisabled = exchangeCount.value == 0,
                    rightBtnDisabled = exchangeCount.value >= walkingCount.value / 1000,
                    leftBtnClick = exchangeStepViewModel::decreaseExchangeCount,
                    rightBtnClick = exchangeStepViewModel::increaseExchangeCount,
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.5f)
                        ) {
                            Text(
                                text = "${walkingCount.value - 1000 * exchangeCount.value} 걸음",
                                textAlign = TextAlign.Center,
                                fontFamily = DAL_MU_RI,
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp,
                                color = MongsWhite,
                                maxLines = 1,
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.5f)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.point_icon_pay),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp),
                                contentScale = ContentScale.FillBounds,
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "+ ${chargePayPoint.value}",
                                textAlign = TextAlign.Center,
                                fontFamily = DAL_MU_RI,
                                fontWeight = FontWeight.Light,
                                fontSize = 18.sp,
                                color = MongsWhite,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
            ) {
                BlueButton(
                    text = "환전",
                    width = 70,
                    disable = chargePayPoint.value == 0,
                    onClick = exchangeStepViewModel::exchangeConfirmDialogOpen,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
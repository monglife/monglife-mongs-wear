package com.monglife.mongs.presentation.view.pages.feed

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Text
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.application.mong.vo.SnackVo
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.assets.SnackResourceCode
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.button.SelectButton
import com.monglife.mongs.presentation.view.component.common.pagenation.PageIndicator
import com.monglife.mongs.presentation.view.component.common.textbox.PayPoint
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.dialog.pages.feed.FeedItemDetailDialog
import com.monglife.mongs.presentation.viewmodel.pages.feed.FeedSnackViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainSlotViewModel
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun FeedSnackView(
    navController: NavController,
    feedSnackViewModel: FeedSnackViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainSlotViewModel: MainSlotViewModel = hiltViewModel<MainSlotViewModel>(parentEntry)

    val uiState = feedSnackViewModel.uiState.collectAsState()
    val mongVo = feedSnackViewModel.mongVo.collectAsState()
    val snackVos = feedSnackViewModel.snackVos.collectAsState()
    val snackIndex = remember { mutableIntStateOf(0) }
    val currentSnackVo = remember {
        derivedStateOf {
            if (snackIndex.intValue < snackVos.value.size) {
                snackVos.value[snackIndex.intValue]
            } else null
        }
    }
    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = snackIndex.intValue
            override val pageCount: Int
                get() = snackVos.value.size
        }
    }

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            mongVo.value?.let { mongVo ->
                currentSnackVo.value?.let { snackVo ->
                    FeedSnackContent(
                        modifier = Modifier.zIndex(1f),
                        mongVo = mongVo,
                        snackVo = snackVo,
                        detailDialogOpen = feedSnackViewModel::detailDialogOpen,
                        buyDialogOpen = feedSnackViewModel::buyDialogOpen,
                    )

                    PageIndicator(
                        pageIndicatorState = pageIndicatorState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 5.dp)
                            .zIndex(1f)
                    )

                    Box(
                        modifier = Modifier.zIndex(2f),
                    ) {
                        if (uiState.value.detailDialogOpen) {
                            FeedItemDetailDialog(
                                weight = snackVo.weight,
                                strength = snackVo.strength,
                                satiety = snackVo.satiety,
                                healthy = snackVo.healthy,
                                fatigue = snackVo.fatigue,
                                onClick = feedSnackViewModel::detailDialogClose,
                            )
                        } else if (uiState.value.buyDialogOpen) {
                            ConfirmAndCancelDialog(
                                text = "구매하시겠습니까?",
                                cancel = feedSnackViewModel::buyDialogClose,
                                confirm = { feedSnackViewModel.buySnack(mongId = mongVo.mongId, snackCode = snackVo.snackCode) },
                            )
                        }
                    }

                    SelectButton(
                        modifier = Modifier.zIndex(3f),
                        leftBtnDisabled = snackIndex.intValue == 0,
                        rightBtnDisabled = snackIndex.intValue == snackVos.value.size - 1,
                        leftBtnClick = { snackIndex.intValue = max(0, snackIndex.intValue - 1) },
                        rightBtnClick = { snackIndex.intValue = min(snackIndex.intValue + 1, snackVos.value.size - 1) },
                    )
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        feedSnackViewModel.uiEvent.collect { event ->
            when (event) {
                is FeedSnackViewModel.UiEvent.NavMenu -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.FeedMenu.route, inclusive = false)
                }
                is FeedSnackViewModel.UiEvent.Feed -> {
                    mainSlotViewModel.eatingEvent()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun FeedSnackContent(
    modifier: Modifier = Modifier,
    mongVo: MongVo,
    snackVo: SnackVo,
    detailDialogOpen: () -> Unit,
    buyDialogOpen: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                PayPoint(payPoint = mongVo.payPoint)
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.52f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.6f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.3f)
                    ) {
                        Text(
                            text = snackVo.snackName,
                            textAlign = TextAlign.Center,
                            fontFamily = DAL_MU_RI,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            color = MongsWhite,
                            maxLines = 1,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f)
                    ) {
                        Image(
                            painter = painterResource(SnackResourceCode.getResourceCode(snackVo.snackCode)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = detailDialogOpen,
                                ),
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.28f)
            ) {
                BlueButton(
                    text = "$${snackVo.price}",
                    width = 70,
                    disable = snackVo.price > mongVo.payPoint || !snackVo.isCanBuy,
                    onClick = buyDialogOpen,
                )
            }

            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}
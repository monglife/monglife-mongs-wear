package com.monglife.mongs.presentation.view.pages.battle

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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import com.monglife.mongs.presentation.view.assets.MongsPink200
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.viewmodel.pages.battle.BattleMenuViewModel
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun BattleMenuView(
    navController: NavController,
    battleMenuViewModel: BattleMenuViewModel = hiltViewModel(),
) {
    val uiState = battleMenuViewModel.uiState.collectAsState()
    val matchQueueVo = battleMenuViewModel.matchQueueVo.collectAsState()
    val currentMongVo = battleMenuViewModel.currentMongVo.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            currentMongVo.value?.let {
                if (uiState.value.matchingLoadingBar) {
                    BattleMatchingContent(
                        modifier = Modifier.zIndex(1f),
                        battleMenuViewModel = battleMenuViewModel,
                    )
                } else {
                    BattleMenuContent(
                        modifier = Modifier.zIndex(1f),
                        battleMenuViewModel = battleMenuViewModel,
                    )
                }

                Box(modifier = Modifier.zIndex(2f)) {
                    if (uiState.value.deleteQueueConfirmDialogOpen) {
                        ConfirmAndCancelDialog(
                            text = "매치를\n\n취소하시겠습니까?",
                            confirm = { battleMenuViewModel.deleteQueue(mongId = it.mongId) },
                            cancel = { battleMenuViewModel.deleteQueueConfirmDialogClose() },
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(matchQueueVo.value) {
        matchQueueVo.value?.let {
            battleMenuViewModel.matching(
                matchId = it.matchId,
                playerId = it.playerId,
            )
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        battleMenuViewModel.uiEvent.collect { event ->
            when (event) {
                is BattleMenuViewModel.UiEvent.NavMatch -> {
                    navController.navigate(route = "${RouterPath.BattleMatch.route}/${event.matchId}/${event.playerId}") {
                        popUpTo(RouterPath.BattleMenu.route) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun BattleMatchingContent(
    modifier: Modifier = Modifier,
    battleMenuViewModel: BattleMenuViewModel,
) {
    val currentMongVo = battleMenuViewModel.currentMongVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
            ) {
                LoadingBar()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
            ) {
                currentMongVo.value?.let {
                    BlueButton(
                        text = "매칭 취소",
                        width = 100,
                        onClick = battleMenuViewModel::deleteQueueConfirmDialogOpen,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BattleMenuContent(
    modifier: Modifier = Modifier,
    battleMenuViewModel: BattleMenuViewModel,
) {
    val matchRewardVo = battleMenuViewModel.matchRewardVo.collectAsState()
    val currentMongVo = battleMenuViewModel.currentMongVo.collectAsState()

    currentMongVo.value?.let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { battleMenuViewModel.createQueue(mongId = it.mongId) },
                ),
        ) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.25f)
                ) {
                    Text(
                        text = "Mongs",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 18.sp,
                        color = MongsPink200,
                        maxLines = 1,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.txt_battle),
                        contentDescription = null,
                        modifier = Modifier
                            .height(45.dp)
                            .width(170.dp),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.15f)
                ) {
                    Text(
                        text = "터치해서 배틀하기",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 13.sp,
                        color = MongsPink200,
                        maxLines = 1,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.point_icon_pay),
                        contentDescription = null,
                        modifier = Modifier
                            .height(20.dp)
                            .width(20.dp),
                        contentScale = ContentScale.FillBounds,
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    matchRewardVo.value?.let { matchRewardVo ->
                        Text(
                            text = "- ${matchRewardVo.bettingPayPoint}",
                            textAlign = TextAlign.Center,
                            fontFamily = DAL_MU_RI,
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp,
                            color = MongsWhite,
                            maxLines = 1,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
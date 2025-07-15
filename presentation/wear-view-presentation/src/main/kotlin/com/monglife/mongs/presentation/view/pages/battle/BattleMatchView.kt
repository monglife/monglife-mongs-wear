package com.monglife.mongs.presentation.view.pages.battle

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
import com.monglife.mongs.domain.battle.enums.MatchPickCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.BattleBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.pages.match.HpBar
import com.monglife.mongs.presentation.view.component.pages.match.MatchPlayer
import com.monglife.mongs.presentation.view.dialog.pages.match.MatchOverDialog
import com.monglife.mongs.presentation.view.dialog.pages.match.MatchPickDialog
import com.monglife.mongs.presentation.viewmodel.pages.battle.BattleMatchViewModel
import com.mongs.wear.presentation.view.wear.R
import kotlin.math.max

private const val MAX_ROUND = 10

@Composable
internal fun BattleMatchView(
    navController: NavController,
    battleMatchViewModel: BattleMatchViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
    matchId: Long?,
    playerId: String?,
) {
    val uiState = battleMatchViewModel.uiState.collectAsState()
    val matchRewardVo = battleMatchViewModel.matchRewardVo.collectAsState()
    val matchVo = battleMatchViewModel.matchVo.collectAsState()
    val matchPlayerVo = battleMatchViewModel.matchPlayerVo.collectAsState()
    val targetMatchPlayerVo = battleMatchViewModel.targetMatchPlayerVo.collectAsState()
    val winMatchPlayerVo = battleMatchViewModel.winMatchPlayerVo.collectAsState()

    Box {
        BattleBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            if (uiState.value.enteringLoadingBar) {
                LaunchedEffect(Unit) {
                    battleMatchViewModel.enter(
                        matchId = matchId,
                        playerId = playerId,
                    )
                }
                BattleEnteringContent(modifier = Modifier.zIndex(1f))
            } else {
                matchVo.value?.let {
                    matchPlayerVo.value?.let { matchPlayerVo ->
                        targetMatchPlayerVo.value?.let { targetMatchPlayerVo ->
                            BattleMatchContent(
                                modifier = Modifier.zIndex(1f),
                                battleMatchViewModel = battleMatchViewModel,
                            )

                            Box(modifier = Modifier.zIndex(2f)) {
                                if (uiState.value.pickDialogOpen) {
                                    MatchPickDialog(
                                        onAttackClick = {
                                            battleMatchViewModel.pick(
                                                matchId = it.matchId,
                                                playerId = matchPlayerVo.playerId,
                                                targetPlayerId = targetMatchPlayerVo.playerId,
                                                pickCode = MatchPickCode.MATCH_PICK_ATTACK,
                                            )
                                        },
                                        onDefenceClick = {
                                            battleMatchViewModel.pick(
                                                matchId = it.matchId,
                                                playerId = matchPlayerVo.playerId,
                                                targetPlayerId = matchPlayerVo.playerId,
                                                pickCode = MatchPickCode.MATCH_PICK_DEFENCE,
                                            )
                                        },
                                        onHealClick = {
                                            battleMatchViewModel.pick(
                                                matchId = it.matchId,
                                                playerId = matchPlayerVo.playerId,
                                                targetPlayerId = matchPlayerVo.playerId,
                                                pickCode = MatchPickCode.MATCH_PICK_HEAL,
                                            )
                                        }
                                    )
                                } else if (uiState.value.endDialogOpen) {
                                    matchRewardVo.value?.let { matchRewardVo ->
                                        winMatchPlayerVo.value?.let { winMatchPlayerVo ->
                                            MatchOverDialog(
                                                battlePayPoint = matchRewardVo.rewardPayPoint,
                                                matchPlayerVo = matchPlayerVo,
                                                winnerMatchPlayer = winMatchPlayerVo,
                                                onMatchEndClick = battleMatchViewModel::exit,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(matchVo.value) {
        matchVo.value?.let {
            battleMatchViewModel.nextRound()
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        battleMatchViewModel.uiEvent.collect { event ->
            when (event) {
                is BattleMatchViewModel.UiEvent.NavMenu -> {
                    if (event.message.isNotBlank()) {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun BattleMatchContent(
    modifier: Modifier,
    battleMatchViewModel: BattleMatchViewModel,
) {
    val uiState = battleMatchViewModel.uiState.collectAsState()
    val matchVo = battleMatchViewModel.matchVo.collectAsState()
    val matchPlayerVo = battleMatchViewModel.matchPlayerVo.collectAsState()
    val matchPlayerMaxHp = battleMatchViewModel.matchPlayerMaxHp.collectAsState()
    val targetMatchPlayerVo = battleMatchViewModel.targetMatchPlayerVo.collectAsState()
    val targetMatchPlayerMaxHp = battleMatchViewModel.targetMatchPlayerMaxHp.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            matchVo.value?.let { matchVo ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                ) {
                    Spacer(modifier = Modifier.width(15.dp))

                    targetMatchPlayerVo.value?.let {
                        MatchPlayer(
                            matchEffect = !uiState.value.pickDialogOpen,
                            matchPlayerVo = it,
                            effectAlignment = Alignment.BottomStart,
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                ) {
                    matchPlayerVo.value?.let {
                        HpBar(
                            hp = it.hp.toFloat(),
                            maxHp = matchPlayerMaxHp.value,
                        )
                    }

                    Text(
                        text = "${matchVo.round}/${MAX_ROUND}",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 20.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )

                    targetMatchPlayerVo.value?.let {
                        HpBar(
                            hp = it.hp.toFloat(),
                            maxHp = targetMatchPlayerMaxHp.value,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                ) {
                    Spacer(modifier = Modifier.width(15.dp))

                    matchPlayerVo.value?.let {
                        MatchPlayer(
                            matchEffect = !uiState.value.pickDialogOpen,
                            matchPlayerVo = it,
                            effectAlignment = Alignment.TopEnd,
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))

                    Image(
                        painter = painterResource(R.drawable.txt_me),
                        contentDescription = null,
                        modifier = Modifier
                            .height(20.dp)
                            .width(40.dp),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
    }
}

@Composable
private fun BattleEnteringContent(
    modifier: Modifier,
) {
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
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
            ) {
                Text(
                    text = "배틀 입장중",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 18.sp,
                    color = MongsWhite,
                    maxLines = 1,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
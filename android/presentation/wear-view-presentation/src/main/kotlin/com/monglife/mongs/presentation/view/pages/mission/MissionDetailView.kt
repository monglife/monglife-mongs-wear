package com.monglife.mongs.presentation.view.pages.mission

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.monglife.mongs.application.mong.vo.MissionRewardVo
import com.monglife.mongs.application.mong.vo.MissionVo
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.enums.MissionRewardTypeCode
import com.monglife.mongs.domain.mong.enums.MissionStateCode
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.FoodResourceCode
import com.monglife.mongs.presentation.view.assets.MongsDarkGray
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.assets.SnackResourceCode
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.bar.ProgressIndicator
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.viewmodel.pages.mission.MissionViewModel
import com.mongs.presentation.view.wear.R

/**
 * 미션 상세 화면. 보상 수령도 여기서 한다.
 *
 * @param accountMissionId 경로 인자. 목록과 같은 ViewModel 을 보므로 서버를 다시 부르지 않는다.
 */
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun MissionDetailView(
    navController: NavController,
    accountMissionId: Long?,
    context: Context = LocalContext.current,
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.MissionNested.route) }
    val missionViewModel: MissionViewModel = hiltViewModel(parentEntry)

    val uiState = missionViewModel.uiState.collectAsStateWithLifecycle()
    val missionVos = missionViewModel.missionVos.collectAsStateWithLifecycle()
    val currentMongVo = missionViewModel.currentMongVo.collectAsStateWithLifecycle()

    val missionVo = missionVos.value.find { it.accountMissionId == accountMissionId }

    /**
     * ViewModel 메서드로 빼지 않는다. 메서드 안에서 StateFlow.value 를 읽으면 컴포지션이
     * 그 상태를 구독하지 않아, 몽이 죽거나 바뀌어도 버튼이 그대로 남는다.
     */
    val canClaim = currentMongVo.value?.let {
        it.stateCode != MongStateCode.DEAD && it.stateCode != MongStateCode.DELETE
    } ?: false

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar || missionVo == null) {
            LoadingBar()
        } else {
            // 진행도는 메인의 경험치 링과 같은 자리(화면 테두리)에 둔다
            Box(modifier = Modifier.zIndex(0f)) {
                ProgressIndicator(
                    progress = { missionVo.progressRatio * 100f },
                    indicatorColor = MongsYellow,
                )
            }

            Box(modifier = Modifier.zIndex(1f)) {
                MissionDetailContent(
                    missionVo = missionVo,
                    canClaim = missionVo.isClaimable && canClaim,
                    onClaimClick = missionViewModel::claimConfirmDialogOpen,
                )
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.confirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        // 보상이 몽 소유라 어느 몽에게 가는지 밝혀 준다.
                        // 슬롯이 여러 개면 "왜 저 몽이 받았지" 가 생긴다.
                        text = "${currentMongVo.value?.name ?: "선택된 몽"} 에게 지급됩니다\n보상을 받으시겠습니까?",
                        cancel = missionViewModel::claimConfirmDialogClose,
                        confirm = {
                            currentMongVo.value?.let {
                                missionViewModel.claimMissionReward(
                                    accountMissionId = missionVo.accountMissionId,
                                    mongId = it.mongId,
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
        missionViewModel.uiEvent.collect { event ->
            when (event) {
                is MissionViewModel.UiEvent.Claim -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                is MissionViewModel.UiEvent.NavMain -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun MissionDetailContent(
    modifier: Modifier = Modifier,
    missionVo: MissionVo,
    canClaim: Boolean,
    onClaimClick: () -> Unit,
) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        ScalingLazyColumn(
            contentPadding = PaddingValues(vertical = 40.dp, horizontal = 16.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState,
            autoCentering = null,
        ) {
            item {
                Text(
                    text = missionVo.title,
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    color = MongsWhite,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            missionVo.description?.let { description ->
                item {
                    Text(
                        text = description,
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = MongsDarkGray,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }
            }

            item {
                Text(
                    text = progressTextOf(missionVo = missionVo),
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = if (missionVo.isClaimable) MongsYellow else MongsWhite,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                )
            }

            item {
                Text(
                    text = "보상",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = MongsDarkGray,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                )
            }

            for (rewardVo in missionVo.rewards) {
                item {
                    MissionRewardRow(rewardVo = rewardVo)
                }
            }

            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    val isClaimed = missionVo.stateCode == MissionStateCode.CLAIMED

                    BlueButton(
                        text = if (isClaimed) "수령 완료" else "받기",
                        // 글자 수에 맞춰 넓힌다. BlueButton 은 좌우 10dp 를 먹고 maxLines 가 1 이라
                        // 좁으면 말줄임 없이 잘린다 - 70dp 에서는 "수령 완" 까지만 나왔다.
                        // 90dp 는 같은 모양(2글자+공백+2글자)인 "슬롯 선택" 버튼과 맞춘 값이다.
                        width = if (isClaimed) 90 else 70,
                        disable = !canClaim,
                        onClick = onClaimClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionRewardRow(
    modifier: Modifier = Modifier,
    rewardVo: MissionRewardVo,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        rewardIconOf(rewardVo = rewardVo)?.let { icon ->
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = rewardTextOf(rewardVo = rewardVo),
            textAlign = TextAlign.Center,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 14.sp,
            color = MongsWhite,
            maxLines = 1,
        )
    }
}

/**
 * 경험치에는 전용 아이콘이 없어 글자로만 보여 준다.
 */
private fun rewardIconOf(rewardVo: MissionRewardVo): Int? = when (rewardVo.rewardTypeCode) {
    MissionRewardTypeCode.PAY_POINT -> R.drawable.point_icon_pay
    MissionRewardTypeCode.STAR_POINT -> R.drawable.point_icon_star
    MissionRewardTypeCode.EXP -> null
    MissionRewardTypeCode.INVENTORY -> rewardVo.rewardCode?.let { rewardCode ->
        when (rewardVo.inventoryTypeCode) {
            InventoryTypeCode.FOOD -> FoodResourceCode.getResourceCode(code = rewardCode)
            InventoryTypeCode.SNACK -> SnackResourceCode.getResourceCode(code = rewardCode)
            else -> null
        }
    }
}

private fun rewardTextOf(rewardVo: MissionRewardVo): String = when (rewardVo.rewardTypeCode) {
    MissionRewardTypeCode.EXP -> "경험치 +${rewardVo.amount}"
    MissionRewardTypeCode.INVENTORY -> "x${rewardVo.amount}"
    else -> "+${rewardVo.amount}"
}

private fun progressTextOf(missionVo: MissionVo): String {
    val progress = "${missionVo.progressCount} / ${missionVo.goalCount}"

    return when (missionVo.stateCode) {
        MissionStateCode.CLAIMABLE -> "$progress · 받을 수 있음"
        MissionStateCode.CLAIMED -> "$progress · 수령 완료"
        MissionStateCode.IN_PROGRESS -> progress
    }
}

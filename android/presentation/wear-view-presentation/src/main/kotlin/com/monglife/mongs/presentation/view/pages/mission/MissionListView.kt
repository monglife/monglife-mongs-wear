package com.monglife.mongs.presentation.view.pages.mission

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import com.monglife.mongs.application.mong.vo.MissionVo
import com.monglife.mongs.domain.mong.enums.MissionCycleCode
import com.monglife.mongs.domain.mong.enums.MissionStateCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsDarkGray
import com.monglife.mongs.presentation.view.assets.MongsPurple
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.pages.mission.MissionChip
import com.monglife.mongs.presentation.viewmodel.pages.mission.MissionViewModel

/**
 * 주기별 미션 목록 화면.
 *
 * @param cycleCode 경로 인자. 이 앱은 navArgument 를 쓰지 않고 경로 세그먼트를 문자열로 받는다.
 */
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun MissionListView(
    navController: NavController,
    cycleCode: String?,
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.MissionNested.route) }
    val missionViewModel: MissionViewModel = hiltViewModel(parentEntry)

    val uiState = missionViewModel.uiState.collectAsStateWithLifecycle()

    // 알 수 없는 코드가 들어오면 일간으로 떨어뜨린다. 경로를 손으로 만드는 구조라 방어해 둔다.
    val missionCycleCode = remember(cycleCode) {
        runCatching { MissionCycleCode.valueOf(cycleCode ?: "") }.getOrDefault(MissionCycleCode.DAILY)
    }

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                MissionListContent(
                    navController = navController,
                    missionViewModel = missionViewModel,
                    cycleCode = missionCycleCode,
                )
            }
        }
    }
}

@Composable
private fun MissionListContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    missionViewModel: MissionViewModel,
    cycleCode: MissionCycleCode,
) {
    val missionVos = missionViewModel.missionVos.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 1)

    val missions = missionVos.value.filter { it.cycleCode == cycleCode }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        PositionIndicator(scalingLazyListState = listState)

        ScalingLazyColumn(
            contentPadding = PaddingValues(vertical = 60.dp, horizontal = 6.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState,
            autoCentering = null,
        ) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Text(
                        text = cycleCode.description,
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            }

            for (missionVo in missions) {
                item {
                    MissionChip(
                        label = missionVo.title,
                        secondaryLabel = secondaryLabelOf(missionVo = missionVo),
                        progressRatio = missionVo.progressRatio,
                        fontColor = fontColorOf(stateCode = missionVo.stateCode),
                        progressColor = progressColorOf(stateCode = missionVo.stateCode),
                        onClick = {
                            navController.navigate(
                                "${RouterPath.MissionDetail.route}/${missionVo.accountMissionId}"
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * 제목 색. 수령 가능해도 흰색을 유지한다 - 줄 전체가 노래지면
 * 정작 "받을 수 있음" 이라는 신호가 묻힌다. 수령 완료만 흐리게 죽인다.
 */
private fun fontColorOf(stateCode: MissionStateCode): Color = when (stateCode) {
    MissionStateCode.CLAIMED -> MongsDarkGray
    MissionStateCode.CLAIMABLE, MissionStateCode.IN_PROGRESS -> MongsWhite
}

/**
 * 채움은 글자색과 따로 둔다. 진행 중 항목의 글자는 흰색이어야 읽히는데,
 * 그 흰색으로 배경까지 칠하면 채운 자리가 그냥 회색으로 보여 진행도가 눈에 안 띈다.
 */
private fun progressColorOf(stateCode: MissionStateCode): Color = when (stateCode) {
    MissionStateCode.CLAIMABLE -> MongsYellow
    MissionStateCode.CLAIMED -> MongsDarkGray
    MissionStateCode.IN_PROGRESS -> MongsPurple
}

private fun secondaryLabelOf(missionVo: MissionVo): AnnotatedString = buildAnnotatedString {
    append("${missionVo.progressCount} / ${missionVo.goalCount}")

    when (missionVo.stateCode) {
        // 받을 수 있다는 것만 노랗게. 이 줄에서 눈에 걸려야 하는 것은 이 한 조각이다
        MissionStateCode.CLAIMABLE -> withStyle(SpanStyle(color = MongsYellow)) { append(" · 받을 수 있음") }
        MissionStateCode.CLAIMED -> append(" · 수령 완료")
        MissionStateCode.IN_PROGRESS -> {}
    }
}

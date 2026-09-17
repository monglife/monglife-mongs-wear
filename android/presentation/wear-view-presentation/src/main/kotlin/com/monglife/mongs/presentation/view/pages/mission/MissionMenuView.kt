package com.monglife.mongs.presentation.view.pages.mission

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsPurple
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.pages.mission.MissionChip
import com.monglife.mongs.presentation.viewmodel.pages.mission.MissionViewModel
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast

/**
 * 미션 주기 선택 화면.
 *
 * 워치에서 25개를 한 목록으로 굴리지 않으려고 주기를 먼저 고르게 한다.
 */
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun MissionMenuView(
    navController: NavController,
    context: Context = LocalContext.current,
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.MissionNested.route) }
    val missionViewModel: MissionViewModel = hiltViewModel(parentEntry)

    val uiState = missionViewModel.uiState.collectAsStateWithLifecycle()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                MissionMenuContent(navController = navController, missionViewModel = missionViewModel)
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        missionViewModel.uiEvent.collect { event ->
            when (event) {
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
private fun MissionMenuContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    missionViewModel: MissionViewModel,
) {
    val missionVos = missionViewModel.missionVos.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 1)

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
                        text = "미션",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            }

            for (cycleCode in MissionCycleCode.entries) {
                item {
                    val missions = missionVos.value.filter { it.cycleCode == cycleCode }
                    val claimableCount = missions.count { it.isClaimable }
                    val clearedCount = missions.count { it.progressCount >= it.goalCount }

                    MissionChip(
                        label = cycleCode.description,
                        secondaryLabel = summaryOf(missions = missions, claimableCount = claimableCount),
                        // 목록 화면과 같은 규칙으로 채운다. 여기서는 그 주기의 "달성한 미션 비율" 이다
                        progressRatio = if (missions.isEmpty()) 0f else clearedCount.toFloat() / missions.size,
                        fontColor = MongsWhite,
                        // 받을 것이 있는 주기를 눈에 띄게 한다
                        progressColor = if (claimableCount > 0) MongsYellow else MongsPurple,
                        onClick = {
                            navController.navigate("${RouterPath.MissionList.route}/${cycleCode.name}")
                        },
                    )
                }
            }
        }
    }
}

/**
 * 요약 문구. 받을 것이 있다는 부분만 노랗게 띄운다 -
 * 줄 전체를 노랗게 하면 주기 이름까지 물들어 무엇이 급한지 되레 안 보인다.
 */
private fun summaryOf(missions: List<MissionVo>, claimableCount: Int): AnnotatedString = buildAnnotatedString {
    if (missions.isEmpty()) {
        append("미션 없음")
        return@buildAnnotatedString
    }

    val clearedCount = missions.count { it.progressCount >= it.goalCount }
    append("달성 $clearedCount/${missions.size}")

    if (claimableCount > 0) {
        withStyle(SpanStyle(color = MongsYellow)) {
            append(" · 받을 수 있음 $claimableCount")
        }
    }
}

package com.monglife.mongs.presentation.view.pages.guide

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.button.CircleImageButton
import com.monglife.mongs.presentation.view.component.common.charactor.Mong
import com.monglife.mongs.presentation.viewmodel.pages.guide.GuideViewModel
import com.mongs.presentation.view.wear.R

/**
 * 최초 진입 가이드.
 *
 * 화면에 뜨는 몽은 서버에 없는 임시 몽이다. 계정에 몽이 하나도 없는 첫 사용자에게도
 * "이런 게 있다" 를 보여 주려는 화면이라 서버를 부르지 않는다.
 *
 * 마지막 단계의 '시작하기' 를 누르면 실제 메인 화면으로 간다. 그 화면에는 당연히 몽이 없다.
 */
@Composable
internal fun GuideView(
    navController: NavController,
    guideViewModel: GuideViewModel = hiltViewModel(),
) {
    val currentStep = guideViewModel.currentStep.collectAsStateWithLifecycle()
    val stepIndex = guideViewModel.stepIndex.collectAsStateWithLifecycle()

    Box {
        DefaultBackground()

        Box(modifier = Modifier.zIndex(1f)) {
            GuideContent(
                step = currentStep.value,
                isLastStep = stepIndex.value >= guideViewModel.stepCount - 1,
                onNextClick = guideViewModel::next,
            )
        }
    }

    /**
     * 들어올 때마다 1단계부터 시작한다.
     * 도움말에서 다시 열었을 때 지난번에 보던 단계가 그대로 남아 있으면 안 된다.
     */
    LaunchedEffect(Unit) {
        guideViewModel.initialize()
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        guideViewModel.uiEvent.collect { event ->
            when (event) {
                is GuideViewModel.UiEvent.Finish -> {
                    /**
                     * 최초 실행이면 가이드가 시작 목적지라 뒤로 갈 곳이 없다. 이때만 메인으로 넘긴다.
                     * 도움말에서 '다시 보기' 로 들어온 경우에는 메인을 또 쌓지 않고 도움말로 돌아간다.
                     */
                    if (navController.previousBackStackEntry == null) {
                        navController.navigate(RouterPath.Main.route) {
                            popUpTo(RouterPath.Guide.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }

                else -> {}
            }
        }
    }
}

/** 흐려진 몽의 투명도. 자리는 지키되 시선은 아래 줄로 가게 하는 정도. */
private const val MONG_DIMMED_ALPHA = 0.4f

/**
 * 240dp 원형 화면에서 몽 + 줄 두 개가 모두 들어가는 높이.
 * 단계가 바뀌어도 몽이 위아래로 움직이지 않도록 높이를 고정한다.
 */
private const val STAGE_HEIGHT = 124

/**
 * 몽만 나오는 첫 단계. 아래가 비어 있으니 크게 쓴다.
 * 더 키우면 원형 화면 위쪽 가장자리에 잘린다.
 */
private const val MONG_RATIO_LARGE = 0.6f

/** 아래 줄이 올라온 뒤. 자리를 내주고 작아진다. */
private const val MONG_RATIO_SMALL = 0.38f

/** 한 줄만 보일 때. 실기기는 에뮬레이터보다 작아 이 정도는 되어야 아이콘이 읽힌다. */
private const val BUTTON_SIZE_SINGLE = 46

/** 마지막 단계에서 두 줄을 한꺼번에 보여 줄 때. */
private const val BUTTON_SIZE_ALL = 28

@Composable
private fun GuideContent(
    modifier: Modifier = Modifier,
    step: GuideViewModel.GuideStep,
    isLastStep: Boolean,
    onNextClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            GuideStage(
                target = step.target,
                modifier = Modifier.height(STAGE_HEIGHT.dp),
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = step.title,
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsYellow,
                        maxLines = 1,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.description,
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = MongsWhite,
                        maxLines = 2,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                BlueButton(
                    text = if (isLastStep) "시작하기" else "다음",
                    width = 78,
                    onClick = onNextClick,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 무대. 몽은 항상 맨 위에 그대로 두고, 아래 줄은 단계가 넘어갈 때 하나씩 드러난다.
 *
 * 처음부터 전부 깔면 한 화면에 세 덩이가 들어가느라 아이콘이 읽기 힘들 만큼 작아진다.
 * 대신 마지막 '준비 끝' 에서만 두 줄을 작게 같이 보여 줘 전체 그림을 남긴다.
 */
@Composable
private fun GuideStage(
    modifier: Modifier = Modifier,
    target: GuideViewModel.GuideTarget,
) {
    val isAllStep = target == GuideViewModel.GuideTarget.NONE
    val showCare = target == GuideViewModel.GuideTarget.CARE || isAllStep
    val showMenu = target == GuideViewModel.GuideTarget.MENU || isAllStep
    val buttonSize = if (isAllStep) BUTTON_SIZE_ALL else BUTTON_SIZE_SINGLE

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        GuideMong(
            isDimmed = !isAllStep && (showCare || showMenu),
            isAlone = !showCare && !showMenu,
        )

        if (showCare) {
            Spacer(modifier = Modifier.height(6.dp))

            GuideButtonRow(
                size = buttonSize,
                icons = listOf(
                    R.drawable.btn_icon_feed to R.drawable.btn_border_yellow,
                    R.drawable.btn_icon_stroke to R.drawable.btn_border_pink,
                    R.drawable.btn_icon_sleep to R.drawable.btn_border_blue,
                ),
            )
        }

        if (showMenu) {
            Spacer(modifier = Modifier.height(if (showCare) 4.dp else 6.dp))

            GuideButtonRow(
                size = buttonSize,
                icons = listOf(
                    R.drawable.btn_icon_mission to R.drawable.btn_border_yellow,
                    R.drawable.btn_icon_slot_pick to R.drawable.btn_border_red,
                    R.drawable.btn_icon_collection to R.drawable.btn_border_orange,
                ),
            )
        }
    }
}

/**
 * 테두리로 가리키지 않는다. 각 단계에서 그 대상만 또렷하게 남고 나머지는 흐려지므로
 * 어디를 말하는지는 이미 드러나고, 작은 화면에서는 테두리가 자리만 잡아먹는다.
 */
@Composable
private fun GuideMong(
    modifier: Modifier = Modifier,
    isDimmed: Boolean,
    isAlone: Boolean,
) {
    val alpha = animateFloatAsState(
        targetValue = if (isDimmed) MONG_DIMMED_ALPHA else 1f,
        label = "guideMongAlpha",
    )

    // 줄이 올라오는 것과 같이 움직이도록 크기도 애니메이션으로 준다.
    val ratio = animateFloatAsState(
        targetValue = if (isAlone) MONG_RATIO_LARGE else MONG_RATIO_SMALL,
        label = "guideMongRatio",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .alpha(alpha.value)
            .padding(5.dp)
    ) {
        Mong(
            isPng = true,
            mong = MongResourceCode.CH100,
            ratio = ratio.value,
        )
    }
}

@Composable
private fun GuideButtonRow(
    modifier: Modifier = Modifier,
    size: Int,
    icons: List<Pair<Int, Int>>,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        icons.forEachIndexed { index, (icon, border) ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(4.dp))
            }

            // 가이드용 그림이라 눌러도 아무 일도 하지 않는다.
            CircleImageButton(
                icon = icon,
                border = border,
                size = size,
                onClick = {},
            )
        }
    }
}

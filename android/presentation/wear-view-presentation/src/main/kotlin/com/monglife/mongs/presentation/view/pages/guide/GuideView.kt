package com.monglife.mongs.presentation.view.pages.guide

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

/**
 * 달무리 폰트는 하강부가 깊어 Compose 기본값(includeFontPadding = false)으로는
 * <b>마지막 줄의 글자 아랫부분이 잘린다.</b> 두 줄짜리 설명에서 둘째 줄만 잘려 보였다.
 * 폰트 여백을 되살려 글리프가 자기 경계 안에 들어오게 한다.
 */
private val TEXT_WITH_FONT_PADDING = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = true),
)

/** 흐려진 몽의 투명도. 자리는 지키되 시선은 아래 줄로 가게 하는 정도. */
private const val MONG_DIMMED_ALPHA = 0.4f

/** Mong 이 ratio = 1f 에서 그리는 크기. 무대에서 잰 dp 를 ratio 로 되돌릴 때 쓴다. */
private val MONG_BASE_SIZE = 120.dp

/**
 * 이 높이 밑은 "좁은 화면" 으로 본다. Wear OS Small Round 가 192dp 라 여기에 걸리고,
 * Large(227dp)·XL(240dp) 은 걸리지 않는다.
 */
private val COMPACT_HEIGHT = 210.dp

/** 무대에서 몽과 줄, 줄과 줄 사이 간격. */
private val STAGE_ROW_GAP = 4.dp

/** 몽이 원형 화면 위 가장자리에 닿지 않게 남겨 두는 여백. */
private val MONG_MARGIN = 6.dp

@Composable
private fun GuideContent(
    modifier: Modifier = Modifier,
    step: GuideViewModel.GuideStep,
    isLastStep: Boolean,
    onNextClick: () -> Unit,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        /**
         * 무대 높이를 고정값으로 잡으면 기기마다 어긋난다. 240dp 화면에 맞춰 둔 124dp 는
         * 192dp 화면에서 화면의 3분의 2를 먹어, 설명 둘째 줄이 '다음' 버튼에 깔렸다.
         * 순서를 뒤집어 글자와 버튼이 자기 높이만 쓰고 남는 세로를 전부 무대에 준다.
         */
        val compact = maxHeight < COMPACT_HEIGHT

        val titleSize = if (compact) 14 else 16
        val descriptionSize = if (compact) 11 else 12
        val blockGap = if (compact) 4.dp else 8.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = if (compact) 6.dp else 10.dp)
        ) {
            GuideStage(
                target = step.target,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Spacer(modifier = Modifier.height(blockGap))

            Text(
                text = step.title,
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = titleSize.sp,
                lineHeight = (titleSize + 6).sp,
                style = TEXT_WITH_FONT_PADDING,
                color = MongsYellow,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))

            Text(
                text = step.description,
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = descriptionSize.sp,
                lineHeight = (descriptionSize + 5).sp,
                style = TEXT_WITH_FONT_PADDING,
                color = MongsWhite,
                maxLines = 2,
            )

            Spacer(modifier = Modifier.height(blockGap))

            BlueButton(
                text = if (isLastStep) "시작하기" else "다음",
                width = 78,
                height = if (compact) 28 else 30,
                onClick = onNextClick,
            )
        }
    }
}

/**
 * 무대. 몽은 항상 맨 위에 그대로 두고, 아래 줄은 단계가 넘어갈 때 하나씩 드러난다.
 *
 * 처음부터 전부 깔면 한 화면에 세 덩이가 들어가느라 아이콘이 읽기 힘들 만큼 작아진다.
 * 대신 마지막 '준비 끝' 에서만 두 줄을 작게 같이 보여 줘 전체 그림을 남긴다.
 *
 * 크기는 받은 높이에서 나눠 쓴다. 줄이 먼저 자기 몫을 가져가고 나머지를 몽이 전부 쓰므로,
 * 어떤 화면에서도 무대가 받은 높이를 넘지 않는다.
 */
@Composable
private fun GuideStage(
    modifier: Modifier = Modifier,
    target: GuideViewModel.GuideTarget,
) {
    val isAllStep = target == GuideViewModel.GuideTarget.NONE
    val showCare = target == GuideViewModel.GuideTarget.CARE || isAllStep
    val showMenu = target == GuideViewModel.GuideTarget.MENU || isAllStep
    val rowCount = (if (showCare) 1 else 0) + (if (showMenu) 1 else 0)

    BoxWithConstraints(modifier = modifier) {
        /**
         * 한 줄만 보이는 단계는 아이콘이 읽혀야 하니 크게, 두 줄을 같이 보여 주는 마지막
         * 단계는 몽까지 세 덩이가 들어가야 하니 작게 잡는다.
         * 가로로도 세 개가 들어가야 해서 너비로 한 번 더 깎는다.
         */
        val buttonSize = when {
            rowCount == 0 -> 0.dp
            isAllStep -> (maxHeight * 0.22f).coerceIn(22.dp, 30.dp)
            else -> (maxHeight * 0.34f).coerceIn(26.dp, 46.dp)
        }.coerceAtMost((maxWidth - STAGE_ROW_GAP * 2) / 3)

        val mongSize = (maxHeight - (buttonSize + STAGE_ROW_GAP) * rowCount - MONG_MARGIN)
            .coerceAtLeast(24.dp)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
        ) {
            GuideMong(
                size = mongSize,
                isDimmed = !isAllStep && rowCount > 0,
                modifier = Modifier.weight(1f),
            )

            if (showCare) {
                Spacer(modifier = Modifier.height(STAGE_ROW_GAP))

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
                Spacer(modifier = Modifier.height(STAGE_ROW_GAP))

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
}

/**
 * 테두리로 가리키지 않는다. 각 단계에서 그 대상만 또렷하게 남고 나머지는 흐려지므로
 * 어디를 말하는지는 이미 드러나고, 작은 화면에서는 테두리가 자리만 잡아먹는다.
 *
 * 자리(Box)는 무대가 정해 준 대로 고정이고 그림만 커졌다 작아진다. 줄어드는 애니메이션이
 * 끝나기 전에 줄이 올라와도 글자를 덮지 않도록 자리 밖은 잘라 낸다.
 */
@Composable
private fun GuideMong(
    modifier: Modifier = Modifier,
    size: Dp,
    isDimmed: Boolean,
) {
    val alpha = animateFloatAsState(
        targetValue = if (isDimmed) MONG_DIMMED_ALPHA else 1f,
        label = "guideMongAlpha",
    )

    // 줄이 올라오는 것과 같이 움직이도록 크기도 애니메이션으로 준다.
    val mongSize = animateDpAsState(
        targetValue = size,
        label = "guideMongSize",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .alpha(alpha.value)
    ) {
        Mong(
            isPng = true,
            mong = MongResourceCode.CH100,
            ratio = mongSize.value / MONG_BASE_SIZE,
        )
    }
}

@Composable
private fun GuideButtonRow(
    modifier: Modifier = Modifier,
    size: Dp,
    icons: List<Pair<Int, Int>>,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        icons.forEachIndexed { index, (icon, border) ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(STAGE_ROW_GAP))
            }

            // 가이드용 그림이라 눌러도 아무 일도 하지 않는다.
            CircleImageButton(
                icon = icon,
                border = border,
                size = size.value.toInt(),
                onClick = {},
            )
        }
    }
}

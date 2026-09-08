package com.monglife.mongs.presentation.view.component.pages.main.slot

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.mongs.presentation.view.mobile.R

private const val PERIOD_MILLIS = 1800

/**
 * 몽이 서 있는 자리의 그림자와, 눌러도 된다는 표시.
 *
 * 몽을 누르면 돌봄 메뉴가 열리는데 눌러 보기 전에는 알 수 없다.
 * 발밑에서 천천히 퍼져 사라지는 고리를 두면 여기가 반응한다는 게 읽힌다.
 *
 * 애니메이션 값은 Canvas 안에서만 읽는다. 컴포지션에서 읽으면
 * 프레임마다 이 컴포저블 전체가 리컴포지션된다.
 */
@Composable
internal fun TapAffordance(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.mong_shadow),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            alpha = 0.45f,
            modifier = Modifier
                .padding(bottom = MainDimens.GroundPadding)
                .width(MainDimens.MongSize * 0.62f)
                .height(MainDimens.MongSize * 0.11f),
        )

        if (!enabled) return@Box

        val transition = rememberInfiniteTransition(label = "tapHint")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(PERIOD_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "phase",
        )

        Canvas(
            modifier = Modifier
                .padding(bottom = MainDimens.GroundPadding)
                .width(MainDimens.MongSize)
                .height(MainDimens.MongSize * 0.24f),
        ) {
            val t = phase
            val scale = 0.45f + 0.55f * t
            val w = size.width * scale
            val h = size.height * scale
            drawOval(
                color = MongsWhite,
                alpha = (1f - t) * 0.34f,
                topLeft = Offset((size.width - w) / 2f, (size.height - h) / 2f),
                size = Size(w, h),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

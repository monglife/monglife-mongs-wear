package com.monglife.mongs.presentation.view.component.common.bar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.assets.MongsPurple
import com.monglife.mongs.presentation.view.assets.MongsWhite
import kotlin.math.max

/**
 * 진행률을 값이 아니라 람다로 받는다.
 * 값으로 받으면 호출부(화면 최상위)에서 상태를 읽게 되어
 * 진행률이 1초마다 바뀔 때 화면 전체가 리컴포지션된다.
 * Canvas 안에서 읽으면 draw 단계만 다시 돈다.
 *
 * wear 는 베젤을 두르는 원형이었지만, 그건 링이 곧 화면 테두리여서 성립한 형태다.
 * 가로 화면의 비정방형 Box 안에서는 심한 타원이 되므로 가로 막대로 바꾼다.
 *
 * wear 판과 달리 내부에서 fillMaxSize 하지 않는다. 폭은 호출부가 준다.
 */
@Composable
internal fun ProgressIndicator(
    modifier: Modifier = Modifier,
    progress: () -> Float = { 100f },
    indicatorColor: Color = MongsPurple,
    trackColor: Color = MongsWhite.copy(alpha = 0.25f),
    height: Dp = 10.dp,
) {
    Canvas(modifier = modifier.height(height)) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)

        drawRoundRect(color = trackColor, cornerRadius = radius)

        val filled = size.width * (progress() / 100f).coerceIn(0f, 1f)
        if (filled > 0f) {
            drawRoundRect(
                color = indicatorColor,
                size = Size(max(filled, size.height), size.height),
                cornerRadius = radius,
            )
        }
    }
}

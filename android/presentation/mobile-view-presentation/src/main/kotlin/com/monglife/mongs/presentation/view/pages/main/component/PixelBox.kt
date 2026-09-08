package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 도트 스타일 박스 — 채움 + 계단 모서리 테두리.
 *
 * RoundedCornerShape 로 둥글리면 안티에일리어싱된 곡선이 나와서
 * 픽셀 스프라이트, 픽셀 폰트와 따로 논다. 모서리를 한 칸씩 깎아
 * 계단으로 만들면 나머지 아트와 같은 결이 된다.
 *
 * 모양은 "사각형에서 네 모서리 한 칸씩 뺀 것"이고, 테두리는 그 윤곽을 따라간다.
 * dot 한 칸이 곧 픽셀 한 칸이라 값을 키우면 더 굵고 거칠어진다.
 */
internal fun Modifier.pixelBox(
    fill: Color,
    border: Color,
    dot: Dp = 3.dp,
): Modifier = this.drawBehind {
    val u = dot.toPx()
    val w = size.width
    val h = size.height
    if (w < 4 * u || h < 4 * u) return@drawBehind

    // 채움 — 두 밴드를 겹치면 모서리 한 칸이 빠진 사각형이 된다.
    drawRect(color = fill, topLeft = Offset(u, 0f), size = Size(w - 2 * u, h))
    drawRect(color = fill, topLeft = Offset(0f, u), size = Size(w, h - 2 * u))

    // 변
    drawRect(color = border, topLeft = Offset(u, 0f), size = Size(w - 2 * u, u))
    drawRect(color = border, topLeft = Offset(u, h - u), size = Size(w - 2 * u, u))
    drawRect(color = border, topLeft = Offset(0f, u), size = Size(u, h - 2 * u))
    drawRect(color = border, topLeft = Offset(w - u, u), size = Size(u, h - 2 * u))

    // 모서리 계단
    drawRect(color = border, topLeft = Offset(u, u), size = Size(u, u))
    drawRect(color = border, topLeft = Offset(w - 2 * u, u), size = Size(u, u))
    drawRect(color = border, topLeft = Offset(u, h - 2 * u), size = Size(u, u))
    drawRect(color = border, topLeft = Offset(w - 2 * u, h - 2 * u), size = Size(u, u))
}

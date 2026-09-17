package com.monglife.mongs.presentation.view.component.pages.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.assets.MongsPurple

/**
 * 미션 진행도 바.
 *
 * 공용 ProgressIndicator 는 화면 테두리를 도는 원형이라 목록/상세 안에는 맞지 않는다.
 * 배틀의 HpBar 와 같은 선형 모양을 쓴다.
 */
/**
 * @param showTrack 빈 트랙을 그릴지. 목록에서는 끈다 - 진행도 0 인 항목이 줄줄이 있으면
 *                  빈 트랙이 항목 사이 구분선처럼 보여 목록이 표처럼 읽힌다.
 */
@Composable
internal fun MissionProgressBar(
    modifier: Modifier = Modifier,
    progressRatio: Float,
    height: Int = 8,
    barColor: Color = MongsPurple,
    showTrack: Boolean = true,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(height.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(if (showTrack) Color.White.copy(alpha = 0.3f) else Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .height(height.dp)
                .fillMaxWidth(fraction = progressRatio.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(color = barColor)
        ) {}
    }
}

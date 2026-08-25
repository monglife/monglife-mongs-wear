package com.monglife.mongs.presentation.view.component.common.pagenation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.SlotPickDimens

/**
 * 페이지 인디케이터.
 *
 * wear 는 HorizontalPageIndicator + PageIndicatorState 를 감쌌지만 Material2 에는 대응물이 없다.
 * 호출부가 pageOffset 을 항상 0f 로 주므로 보간이 필요 없어 인덱스와 개수만 받는다.
 *
 * 페이지는 최대 3개다 (SlotPickViewModel 이 슬롯을 3개까지만 만든다).
 * 넘침 처리를 넣지 않은 건 그래서다.
 *
 * dot 은 원이 아니라 사각형이다 - 곡선은 안티에일리어싱되어 픽셀 스프라이트와 따로 논다.
 * 선택색도 wear 의 MongsNavy 가 아니다. 검은 워치 배경에서는 보였지만 어두운 맵 위에서는 안 보인다.
 */
@Composable
internal fun PageIndicator(
    modifier: Modifier = Modifier,
    selectedPage: Int,
    pageCount: Int,
    dotSize: Dp = SlotPickDimens.Dot,
    spacing: Dp = SlotPickDimens.DotGap,
    selectedColor: Color = MongsWhite,
    unselectedColor: Color = MongsWhite.copy(alpha = 0.3f),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(if (page == selectedPage) selectedColor else unselectedColor)
            )
        }
    }
}

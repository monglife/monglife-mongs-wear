package com.monglife.mongs.presentation.view.component.pages.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.assets.MongsRed
import com.monglife.mongs.presentation.view.assets.MongsWhite
import kotlin.math.ceil

/** 체력을 나눌 칸 수. 한 칸이 곧 최대 체력의 10% 다 */
private const val SEGMENT_COUNT = 10

/** 테두리 두께. 얇으면 둥글게 보여 도트 느낌이 죽는다 */
private const val BORDER_WIDTH = 2

/** 칸 사이 간격 */
private const val SEGMENT_GAP = 1

/** 빈 칸. 완전히 비우면 바의 끝을 알 수 없어 어둡게만 깐다 */
private val EMPTY_SEGMENT_COLOR = Color.Black.copy(alpha = 0.45f)

/**
 * 배틀 체력 바.
 *
 * <p>둥근 알약이 아니라 각진 테두리 안에 칸을 나눈 모양이다. 나머지 화면이 전부 도트
 * 그림인데 체력 바만 매끈한 알약이라 겉돌았다.
 *
 * <p>칸 수만큼 끊어서 보여 주므로 잔 데미지는 칸을 넘기기 전까지 화면에 안 나타난다.
 * 대신 남은 체력이 몇 칸인지가 한눈에 읽힌다 - 고전 도트 게임의 체력 바와 같은 셈법이다.
 */
@Composable
fun HpBar(
    modifier: Modifier = Modifier,
    hp: Float,
    maxHp: Float,
    height: Int = 20,
    width: Int = 65,
    segmentCount: Int = SEGMENT_COUNT,
) {
    val ratio = if (maxHp <= 0f) 0f else (hp / maxHp).coerceIn(0f, 1f)

    /**
     * 칸 수는 올림이다. 버림으로 하면 체력이 한 칸에 못 미치게 남았을 때 바가 완전히
     * 비어 보여, 아직 살아 있는데 죽은 것처럼 읽힌다.
     */
    val filledCount = if (ratio <= 0f) 0 else ceil(ratio * segmentCount).toInt()

    Row(
        horizontalArrangement = Arrangement.spacedBy(SEGMENT_GAP.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(height.dp)
            .width(width.dp)
            .border(width = BORDER_WIDTH.dp, color = MongsWhite)
            .padding(BORDER_WIDTH.dp),
    ) {
        repeat(segmentCount) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (index < filledCount) MongsRed else EMPTY_SEGMENT_COLOR),
            )
        }
    }
}

package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.component.common.button.LabeledCircleButton
import com.mongs.presentation.view.mobile.R
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val RADIUS = 130
private const val SLOT_WIDTH = 76
private val ACTION_SIZE = MainDimens.ActionSize

/**
 * 몽을 눌렀을 때 몽 주위에 반원으로 펼쳐지는 돌봄 메뉴.
 *
 * wear 는 InteractionDialog 로 화면을 덮고 버튼을 세로로 쌓았다. 원형 화면에서는
 * 그 방법밖에 없었지만, 가로 폰에서는 몽을 가리지 않고 주위에 두를 수 있다.
 *
 * 완전한 원이 아니라 위쪽 반원인 이유는 아래쪽 절반이 지면과 하단 바에 막히기
 * 때문이다. 왼쪽(180도)에서 위(90도)를 지나 오른쪽(0도)까지 45도 간격 5개.
 *
 * disable 술어는 wear InteractionDialog 원문 그대로다.
 */
@Composable
internal fun InteractionRing(
    modifier: Modifier = Modifier,
    centerInRoot: Offset,
    level: Int,
    stateCode: MongStateCode,
    isSleep: Boolean,
    onFeed: () -> Unit,
    onStroke: () -> Unit,
    onSleep: () -> Unit,
    onPoopClean: () -> Unit,
    onInventory: () -> Unit,
    onClose: () -> Unit,
) {
    val density = LocalDensity.current
    val radiusPx = with(density) { RADIUS.dp.toPx() }
    // 슬롯은 버튼(56) + 라벨 한 줄이라 세로가 더 길다. 원의 기준점은 버튼 중심이어야 하므로
    // 가로는 슬롯 폭의 절반, 세로는 버튼 높이의 절반만큼 되돌린다.
    val halfSlotWidthPx = with(density) { (SLOT_WIDTH / 2).dp.toPx() }
    val halfButtonPx = with(density) { (ACTION_SIZE / 2).dp.toPx() }

    val items = listOf(
        Slot(R.drawable.btn_icon_feed, R.drawable.btn_border_yellow, "먹이",
            level == 0 || isSleep || stateCode in listOf(MongStateCode.DEAD, MongStateCode.DELETE), onFeed),
        Slot(R.drawable.btn_icon_stroke, R.drawable.btn_border_pink, "쓰다듬",
            level == 0 || isSleep || stateCode != MongStateCode.NORMAL, onStroke),
        Slot(R.drawable.btn_icon_sleep, R.drawable.btn_border_blue, if (isSleep) "깨우기" else "재우기",
            level == 0 || stateCode != MongStateCode.NORMAL, onSleep),
        Slot(R.drawable.btn_icon_poop_clean, R.drawable.btn_border_purple, "청소",
            level == 0 || isSleep || stateCode != MongStateCode.NORMAL, onPoopClean),
        Slot(R.drawable.btn_icon_inventory, R.drawable.btn_border_green, "가방",
            level == 0 || isSleep || stateCode in listOf(MongStateCode.DEAD, MongStateCode.DELETE), onInventory),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            )
    ) {
        items.forEachIndexed { index, slot ->
            // 180도(왼쪽) -> 90도(위) -> 0도(오른쪽)
            val radians = Math.toRadians(180.0 - index * 45.0)
            val dx = radiusPx * cos(radians).toFloat()
            val dy = -radiusPx * sin(radians).toFloat()

            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            x = (centerInRoot.x + dx - halfSlotWidthPx).roundToInt(),
                            y = (centerInRoot.y + dy - halfButtonPx).roundToInt(),
                        )
                    }
                    .width(SLOT_WIDTH.dp),
            ) {
                LabeledCircleButton(
                    icon = slot.icon,
                    border = slot.border,
                    label = slot.label,
                    disable = slot.disable,
                    onClick = {
                        slot.onClick()
                        onClose()
                    },
                )
            }
        }
    }
}

private data class Slot(
    val icon: Int,
    val border: Int,
    val label: String,
    val disable: Boolean,
    val onClick: () -> Unit,
)

package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MongsDarkYellow
import com.monglife.mongs.presentation.view.assets.MongsPurple
import com.monglife.mongs.presentation.view.assets.MongsRed
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.bar.ProgressIndicator
import com.monglife.mongs.presentation.view.utils.NumberUtil
import com.mongs.presentation.view.mobile.R

/**
 * 상단 HUD — 레벨/경험치, 스타 포인트, 페이 포인트, 걸음 수.
 *
 * 알(level 0) 일 때는 경험치 자리를 부화 타이머가 차지한다.
 */
@Composable
internal fun MainHud(
    modifier: Modifier = Modifier,
    level: Int?,
    expRatio: Float,
    hatchProgress: (() -> Float)? = null,
    hatchLabel: String? = null,
    starPoint: Int,
    payPoint: Int?,
    walkingCount: Int,
    stepAvailable: Boolean,
    onStepClick: () -> Unit,
) {
    val isEgg = hatchProgress != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MainDimens.HudHeight)
            .mainPanel()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = level?.let { "Lv.$it" } ?: "Lv.-",
                fontFamily = DAL_MU_RI,
                fontSize = 15.sp,
                color = MongsWhite.copy(alpha = if (level == null) 0.4f else 1f),
                maxLines = 1,
            )
            ProgressIndicator(
                modifier = Modifier.width(210.dp),
                progress = hatchProgress ?: { expRatio },
                indicatorColor = if (isEgg) MongsDarkYellow else MongsPurple,
            )
            Text(
                text = hatchLabel ?: level?.let { "경험치 ${expRatio.toInt()}%" } ?: "몽 없음",
                fontFamily = DAL_MU_RI,
                fontSize = 13.sp,
                color = MongsWhite.copy(alpha = 0.85f),
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HudChip(icon = R.drawable.point_icon_star, text = NumberUtil.formatAsCurrency(starPoint))

        HudChip(
            icon = R.drawable.point_icon_pay,
            text = payPoint?.let { NumberUtil.formatAsCurrency(it) } ?: "-",
            alpha = if (payPoint == null) 0.4f else 1f,
        )

        /**
         * wear 는 권한이 없으면 전면 다이얼로그를 띄웠다. 단일 화면에서 그러면
         * 화면 전체가 막히므로, 칩 자체를 요청 상태로 바꾸고 탭했을 때 연다.
         */
        HudChip(
            icon = R.drawable.btn_icon_walking,
            text = if (!stepAvailable) "권한 필요" else "${NumberUtil.formatAsCurrency(walkingCount)} 걸음",
            background = if (!stepAvailable) MongsRed.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.3f),
            onClick = onStepClick,
        )
    }
}

@Composable
private fun HudChip(
    icon: Int,
    text: String,
    alpha: Float = 1f,
    background: Color = Color.Black.copy(alpha = 0.3f),
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .background(color = background, shape = RoundedCornerShape(14.dp))
            .let {
                if (onClick == null) it else it.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            alpha = alpha,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            fontFamily = DAL_MU_RI,
            fontSize = 14.sp,
            color = MongsWhite.copy(alpha = alpha),
            maxLines = 1,
        )
    }
}

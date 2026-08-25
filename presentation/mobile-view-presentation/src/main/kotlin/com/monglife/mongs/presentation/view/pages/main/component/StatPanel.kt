package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MongsBlue
import com.monglife.mongs.presentation.view.assets.MongsDarkYellow
import com.monglife.mongs.presentation.view.assets.MongsGreen
import com.monglife.mongs.presentation.view.assets.MongsPink
import com.monglife.mongs.presentation.view.assets.MongsPurple
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.component.common.bar.ProgressIndicator
import com.monglife.mongs.presentation.view.component.common.bar.StatGauge
import com.monglife.mongs.presentation.view.utils.NumberUtil
import com.mongs.presentation.view.mobile.R

/**
 * 좌측 스텟 열 — 레벨/경험치 + 컨디션 4종.
 *
 * 레벨과 경험치는 몽의 성장이고 컨디션은 몽의 상태다. 둘 다 몽에 붙는 값이라
 * 한 열에 모아 둔다. (화면 상단은 계정에 붙는 포인트/걸음 수 자리였다)
 */
@Composable
internal fun StatPanel(
    modifier: Modifier = Modifier,
    currentMongVo: MongVo?,
    hatchProgress: (() -> Float)? = null,
) {
    val dim = currentMongVo == null
    val isEgg = hatchProgress != null

    Column(
        modifier = modifier
            .fillMaxHeight()
            .mainPanel()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 레벨과 경험치는 한 줄로. 세로를 게이지에 몰아준다.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = currentMongVo?.let { "Lv.${it.level}" } ?: "Lv.-",
                fontFamily = DAL_MU_RI,
                fontSize = 18.sp,
                color = MongsWhite.copy(alpha = if (dim) 0.4f else 1f),
                maxLines = 1,
            )
            ProgressIndicator(
                modifier = Modifier.weight(1f),
                progress = hatchProgress ?: { currentMongVo?.expRatio?.toFloat() ?: 0f },
                indicatorColor = (if (isEgg) MongsDarkYellow else MongsPurple)
                    .copy(alpha = if (dim) 0.4f else 1f),
                height = 12.dp,
            )
            Text(
                text = when {
                    isEgg -> "부화"
                    dim -> "-"
                    else -> "${currentMongVo?.expRatio?.toInt()}"
                },
                fontFamily = DAL_MU_RI,
                fontSize = 14.sp,
                color = (if (isEgg) MongsDarkYellow else MongsPurple)
                    .copy(alpha = if (dim) 0.4f else 1f),
                maxLines = 1,
            )
        }

        PanelDivider()

        /**
         * 게이지를 고정 간격으로 쌓으면 패널이 크면 아래가 비고 작으면 마지막이 잘린다.
         * 네 개가 남는 높이를 똑같이 나눠 갖게 해서 항상 아래까지 채운다.
         */
        listOf(
            Gauge(R.drawable.icon_healthy, "체력", currentMongVo?.healthyRatio, MongsPink),
            Gauge(R.drawable.icon_satiety, "포만감", currentMongVo?.satietyRatio, MongsYellow),
            Gauge(R.drawable.icon_strength, "힘", currentMongVo?.strengthRatio, MongsGreen),
            Gauge(R.drawable.icon_fatigue, "피로도", currentMongVo?.fatigueRatio, MongsBlue),
        ).forEach { gauge ->
            StatGauge(
                modifier = Modifier.weight(1f),
                icon = gauge.icon,
                label = gauge.label,
                progress = gauge.ratio?.toFloat() ?: 0f,
                indicatorColor = gauge.color,
                dim = dim,
            )
        }

        PanelDivider()

        /**
         * 페이 포인트는 몽이 벌어들이는 값이라 계정 재화(상단바)가 아니라 여기에 둔다.
         * 표시 전용이다.
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.point_icon_pay),
                contentDescription = null,
                alpha = if (dim) 0.35f else 1f,
                modifier = Modifier.size(22.dp),
            )
            // 상단바 칩과 같이 아이콘 바로 옆에 숫자만 붙인다.
            Text(
                text = currentMongVo?.payPoint?.let { NumberUtil.formatAsCurrency(it) } ?: "-",
                fontFamily = DAL_MU_RI,
                fontSize = 16.sp,
                color = MongsDarkYellow.copy(alpha = if (dim) 0.35f else 1f),
                maxLines = 1,
            )
        }
    }
}

private data class Gauge(
    val icon: Int,
    val label: String,
    val ratio: Double?,
    val color: Color,
)

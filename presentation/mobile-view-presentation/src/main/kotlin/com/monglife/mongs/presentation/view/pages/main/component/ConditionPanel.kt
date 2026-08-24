package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MongsBlue
import com.monglife.mongs.presentation.view.assets.MongsGreen
import com.monglife.mongs.presentation.view.assets.MongsPink
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.component.common.bar.StatGauge
import com.mongs.presentation.view.mobile.R

/**
 * 좌측 컨디션 패널.
 *
 * wear 는 60dp 원형 링 4개를 2x2 로 놓았다. 가로 화면에서 그러면 좌우가 죽고,
 * 무엇보다 4개를 한 번에 훑을 수 없다. 세로로 쌓은 막대가 빠르다.
 */
@Composable
internal fun ConditionPanel(
    modifier: Modifier = Modifier,
    currentMongVo: MongVo?,
) {
    val dim = currentMongVo == null

    Column(
        modifier = modifier
            .width(MainDimens.PanelWidth)
            .fillMaxHeight()
            .mainPanel()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        StatGauge(
            icon = R.drawable.icon_healthy,
            label = "체력",
            progress = currentMongVo?.healthyRatio?.toFloat() ?: 0f,
            indicatorColor = MongsPink,
            dim = dim,
        )
        StatGauge(
            icon = R.drawable.icon_satiety,
            label = "포만감",
            progress = currentMongVo?.satietyRatio?.toFloat() ?: 0f,
            indicatorColor = MongsYellow,
            dim = dim,
        )
        StatGauge(
            icon = R.drawable.icon_strength,
            label = "힘",
            progress = currentMongVo?.strengthRatio?.toFloat() ?: 0f,
            indicatorColor = MongsGreen,
            dim = dim,
        )
        StatGauge(
            icon = R.drawable.icon_fatigue,
            label = "피로도",
            progress = currentMongVo?.fatigueRatio?.toFloat() ?: 0f,
            indicatorColor = MongsBlue,
            dim = dim,
        )
    }
}
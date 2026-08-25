package com.monglife.mongs.presentation.view.component.common.bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite

/**
 * 컨디션 게이지 한 줄.
 *
 * wear 는 60dp 원형 링 4개를 2x2 로 놓았지만, 좌측 패널에서는
 * 아이콘 + 라벨 + 가로 막대가 훨씬 빨리 읽힌다.
 */
@Composable
internal fun StatGauge(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    progress: Float,
    indicatorColor: Color,
    dim: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                alpha = if (dim) 0.35f else 1f,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                fontFamily = DAL_MU_RI,
                fontSize = 16.sp,
                color = MongsWhite.copy(alpha = if (dim) 0.35f else 1f),
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            // 막대만으로는 값을 읽을 수 없다.
            Text(
                text = if (dim) "-" else "${progress.toInt()}",
                fontFamily = DAL_MU_RI,
                fontSize = 16.sp,
                color = indicatorColor.copy(alpha = if (dim) 0.35f else 1f),
                maxLines = 1,
            )
        }

        ProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = { progress },
            indicatorColor = indicatorColor.copy(alpha = if (dim) 0.35f else 1f),
            height = 12.dp,
        )
    }
}

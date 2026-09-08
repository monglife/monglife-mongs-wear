package com.monglife.mongs.presentation.view.component.common.textbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsDarkYellow
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.pages.main.component.pixelBox
import com.monglife.mongs.presentation.view.utils.NumberUtil
import com.mongs.presentation.view.mobile.R

/**
 * wear 는 point_bg 스프라이트를 ContentScale.FillBounds 로 늘려 배경으로 썼다.
 * 폰 배율에서는 그게 필터링되어 흐려지는데, Image 로는 filterQuality 를 줄 수 없다.
 * 상단바 칩과 같은 pixelBox 로 그린다 - 늘리지 않으니 뭉개지지 않고 결도 맞는다.
 */
@Composable
internal fun StarPointBox(
    modifier: Modifier = Modifier,
    height: Int = 40,
    width: Int = 110,
    starPoint: Int = 0,
) {
    Row(
        modifier = modifier
            .height(height.dp)
            .width(width.dp)
            .pixelBox(
                fill = Color.Black.copy(alpha = 0.32f),
                border = MongsWhite.copy(alpha = 0.4f),
            )
            .padding(start = 12.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.point_icon_star),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = NumberUtil.formatAsCurrency(starPoint),
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 16.sp,
            color = MongsDarkYellow,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
    }
}

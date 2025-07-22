package com.monglife.mongs.presentation.view.component.common.textbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsDarkBrown
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun StarPointBox(
    modifier: Modifier = Modifier,
    height: Int = 30,
    width: Int = 80,
    starPoint: Int = 0,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height.dp)
            .width(width.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.point_bg),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .zIndex(1.1f),
            contentScale = ContentScale.FillBounds,
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.zIndex(1.2f).padding(start = 10.dp, end = 10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(0.2f),
            ) {
                Image(
                    painter = painterResource(R.drawable.point_icon_star),
                    contentDescription = null,
                    modifier = Modifier
                        .height(12.dp)
                        .width(12.dp),
                    contentScale = ContentScale.FillBounds,
                )
            }

            Text(
                text = "$starPoint",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = MongsDarkBrown,
                maxLines = 1,
                modifier = Modifier.weight(0.8f)
            )
        }
    }
}

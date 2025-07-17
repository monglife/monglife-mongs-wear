package com.monglife.mongs.presentation.view.component.pages.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.assets.MongsRed

@Composable
fun HpBar(
    modifier: Modifier = Modifier,
    hp: Float,
    maxHp: Float,
    height: Int = 20,
    width: Int = 65,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(height.dp)
            .width(width.dp)
            .clip(CircleShape)
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .height(height.dp)
                .fillMaxWidth(fraction = hp / maxHp)
                .background(color = MongsRed)
        ) {}
    }
}
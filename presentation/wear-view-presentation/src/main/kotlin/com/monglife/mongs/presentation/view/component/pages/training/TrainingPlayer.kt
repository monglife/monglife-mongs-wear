package com.monglife.mongs.presentation.view.component.pages.training

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.assets.MongResourceCode

@Composable
internal fun TrainingPlayer(
    modifier: Modifier = Modifier,
    mongCode: String,
    height: Int,
    width: Int,
) {
    Box(
        modifier = modifier
            .height(height.dp)
            .width(width.dp),
    ) {
        Image(
            painter = painterResource(MongResourceCode.getResource(code = mongCode).pngCode),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    // 좌우 반전을 위해 scaleX -1로 설정
                    scaleX = -1f
                ),
            contentScale = ContentScale.FillBounds,
        )
    }
}
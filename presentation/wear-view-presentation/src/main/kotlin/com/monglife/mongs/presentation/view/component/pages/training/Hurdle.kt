package com.monglife.mongs.presentation.view.component.pages.training

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
internal fun Hurdle(
    modifier: Modifier = Modifier,
    image: Int,
    height: Int,
    width: Int,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height.dp)
            .width(width.dp)
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            contentScale = ContentScale.FillBounds,
        )
    }
}

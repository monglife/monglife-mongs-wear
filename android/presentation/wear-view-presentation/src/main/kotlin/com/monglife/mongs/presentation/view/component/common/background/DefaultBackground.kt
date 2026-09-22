package com.monglife.mongs.presentation.view.component.common.background

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.monglife.mongs.presentation.view.assets.MapResourceCode

@Composable
internal fun DefaultBackground(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(MapResourceCode.MP000.code),
            contentDescription = "DefaultBackground",
            contentScale = ContentScale.Crop
        )
    }
}
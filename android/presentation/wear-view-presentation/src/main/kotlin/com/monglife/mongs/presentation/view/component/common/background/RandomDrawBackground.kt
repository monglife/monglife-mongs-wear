package com.monglife.mongs.presentation.view.component.common.background

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.monglife.mongs.presentation.view.assets.LocalMongsImageLoader
import com.mongs.presentation.view.wear.R

@Composable
internal fun RandomDrawBackground(
    modifier: Modifier = Modifier,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = R.drawable.bg_walking_gif,
                imageLoader = LocalMongsImageLoader.current,
                placeholder = painterResource(R.drawable.bg_walking),
            ),
            contentDescription = "RandomDrawBackground",
        )
    }
}
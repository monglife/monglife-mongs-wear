package com.monglife.mongs.presentation.view.component.common.background

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun RandomDrawBackground(
    modifier: Modifier = Modifier,
) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = R.drawable.bg_walking_gif,
                imageLoader = imageLoader,
                placeholder = painterResource(R.drawable.bg_walking),
            ),
            contentDescription = "MainPagerBackground",
        )
    }
}
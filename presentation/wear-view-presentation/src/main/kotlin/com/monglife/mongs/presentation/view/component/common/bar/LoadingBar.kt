package com.monglife.mongs.presentation.view.component.common.bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun LoadingBar(
    modifier: Modifier = Modifier,
    height: Int = 40,
    width: Int = 40,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        val imageLoader = ImageLoader.Builder(LocalContext.current)
            .components { add(ImageDecoderDecoder.Factory()) }
            .build()
        val loading = R.drawable.icon_loading

        Image(
            painter = rememberAsyncImagePainter(model = loading, imageLoader = imageLoader),
            contentDescription = null,
            modifier = Modifier
                .height(height.dp)
                .width(width.dp)
        )
    }
}
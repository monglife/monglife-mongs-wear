package com.monglife.mongs.presentation.view.component.pages.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import com.mongs.presentation.view.wear.R

@Composable
internal fun PickLoadingBar() {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()
    val loading = R.drawable.icon_loading

    Image(
        painter = rememberAsyncImagePainter(model = loading, imageLoader = imageLoader),
        contentDescription = null,
        modifier = Modifier
            .size(25.dp),
    )
}
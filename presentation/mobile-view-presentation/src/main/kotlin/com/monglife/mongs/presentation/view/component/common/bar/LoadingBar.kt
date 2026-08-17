package com.monglife.mongs.presentation.view.component.common.bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.monglife.mongs.presentation.view.assets.LocalMongsImageLoader
import com.mongs.presentation.view.mobile.R

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
        val loading = R.drawable.icon_loading

        Image(
            painter = rememberAsyncImagePainter(model = loading, imageLoader = LocalMongsImageLoader.current),
            contentDescription = null,
            modifier = Modifier
                .height(height.dp)
                .width(width.dp)
        )
    }
}
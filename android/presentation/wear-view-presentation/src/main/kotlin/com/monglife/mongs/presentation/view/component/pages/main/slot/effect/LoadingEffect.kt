package com.monglife.mongs.presentation.view.component.pages.main.slot.effect

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.monglife.mongs.presentation.view.assets.LocalMongsImageLoader
import com.mongs.presentation.view.wear.R

@Composable
internal fun LoadingEffect(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        val loading = R.drawable.icon_loading

        Image(
            painter = rememberAsyncImagePainter(model = loading, imageLoader = LocalMongsImageLoader.current),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 25.dp)
                .size(25.dp),
        )
    }
}
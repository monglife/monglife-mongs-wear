package com.monglife.mongs.presentation.view.component.pages.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.monglife.mongs.presentation.view.assets.LocalMongsImageLoader
import com.mongs.presentation.view.wear.R

@Composable
internal fun PickLoadingBar() {
    val loading = R.drawable.icon_loading

    Image(
        painter = rememberAsyncImagePainter(model = loading, imageLoader = LocalMongsImageLoader.current),
        contentDescription = null,
        modifier = Modifier
            .size(25.dp),
    )
}
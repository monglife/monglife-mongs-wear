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
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.LocalMongsImageLoader
import com.mongs.presentation.view.mobile.R

@Composable
internal fun PoopCleanEffect(
    modifier: Modifier = Modifier,
) {

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize(),
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = R.drawable.effect_vacuum,
                imageLoader = LocalMongsImageLoader.current
            ),
            contentDescription = "PoopCleanEffect",
            modifier = Modifier
                .size(175.dp)
                .padding(bottom = 29.dp),
        )
    }
}

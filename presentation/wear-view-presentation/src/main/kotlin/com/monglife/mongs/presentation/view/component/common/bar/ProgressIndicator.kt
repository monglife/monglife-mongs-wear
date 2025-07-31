package com.monglife.mongs.presentation.view.component.common.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import com.monglife.mongs.presentation.view.assets.MongsPurple

@Composable
internal fun ProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float = 100f,
    indicatorColor: Color = MongsPurple,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            progress = progress / 100,
            strokeWidth = 4.dp,
            indicatorColor = indicatorColor,
        )
    }
}
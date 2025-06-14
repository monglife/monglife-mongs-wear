package com.monglife.mongs.presentation.view.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Text

@Composable
fun MainView (
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text="테스트", textAlign = TextAlign.Center, color = Color.White)
    }
}


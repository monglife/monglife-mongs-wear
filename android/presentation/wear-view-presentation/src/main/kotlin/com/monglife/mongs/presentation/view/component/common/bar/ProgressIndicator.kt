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

/**
 * 진행률을 값이 아니라 람다로 받는다.
 * 값으로 받으면 호출부(화면 최상위) 에서 상태를 읽게 되어
 * 진행률이 1초마다 바뀔 때 화면 전체가 리컴포지션된다.
 */
@Composable
internal fun ProgressIndicator(
    modifier: Modifier = Modifier,
    progress: () -> Float = { 100f },
    indicatorColor: Color = MongsPurple,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            progress = progress() / 100,
            strokeWidth = 4.dp,
            indicatorColor = indicatorColor,
        )
    }
}
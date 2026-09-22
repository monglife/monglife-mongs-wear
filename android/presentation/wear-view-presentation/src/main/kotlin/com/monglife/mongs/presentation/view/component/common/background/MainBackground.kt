package com.monglife.mongs.presentation.view.component.common.background

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.monglife.mongs.presentation.view.assets.LocalMongsImageLoader
import com.monglife.mongs.presentation.view.assets.MapResourceCode
import com.mongs.presentation.view.wear.R
import kotlin.math.absoluteValue

@Composable
internal fun MainBackground(
    modifier: Modifier = Modifier,
    backgroundMapCode: String?,
    pagerState: PagerState,
    pagerBrightnesses: Array<Float>,
) {
    backgroundMapCode?.let {
        val alpha = remember {
            derivedStateOf {
                val currentPage = pagerState.currentPage
                val ratio = pagerState.currentPageOffsetFraction.coerceIn(-1f, 1f)
                val nextPage = if (ratio < 0) {
                    currentPage - 1
                } else if (ratio > 0) {
                    currentPage + 1
                } else currentPage
                val current = pagerBrightnesses[currentPage]
                val next = pagerBrightnesses[nextPage]
                current + (next - current) * ratio.absoluteValue
            }
        }

        val mapResourceCode = MapResourceCode.getResource(code = it)

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                if (mapResourceCode == MapResourceCode.MP000) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = R.drawable.map_mp000_gif,
                            imageLoader = LocalMongsImageLoader.current,
                            placeholder = painterResource(mapResourceCode.code),
                        ),
                        contentDescription = "MainPagerBackground",
                    )
                } else {
                    Image(
                        painter = painterResource(mapResourceCode.code),
                        contentDescription = "MainPagerBackground",
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        /**
                         * alpha 는 페이저 스크롤 중 프레임마다 바뀐다.
                         * Modifier.background(색상) 로 넘기면 값을 컴포지션 단계에서 읽게 되어
                         * 프레임마다 이 컴포저블 전체가 리컴포지션된다.
                         * drawBehind 안에서 읽으면 draw 단계만 다시 돈다.
                         */
                        .drawBehind {
                            drawRect(color = Color.Black, alpha = alpha.value)
                        }
                        .fillMaxSize()
                        .zIndex(2f)
                )
            }
        }
    } ?: run {
        DefaultBackground()
    }
}

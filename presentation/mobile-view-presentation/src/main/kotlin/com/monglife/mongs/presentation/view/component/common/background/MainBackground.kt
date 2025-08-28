package com.monglife.mongs.presentation.view.component.common.background

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import com.monglife.mongs.presentation.view.assets.MapResourceCode
import com.mongs.presentation.view.mobile.R
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
        val imageLoader = ImageLoader.Builder(LocalContext.current)
            .components { add(ImageDecoderDecoder.Factory()) }
            .build()

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
                            imageLoader = imageLoader,
                            placeholder = painterResource(mapResourceCode.code),
                        ),
                        contentDescription = "MainPagerBackground",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(mapResourceCode.code),
                        contentDescription = "MainPagerBackground",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .background(color = Color.Black.copy(alpha = alpha.value))
                        .fillMaxSize()
                        .zIndex(2f)
                )
            }
        }
    } ?: run {
        DefaultBackground()
    }
}

package com.monglife.mongs.presentation.view.component.common.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.monglife.mongs.presentation.view.assets.LocalMongsImageLoader
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MapResourceCode
import com.mongs.presentation.view.mobile.R
import kotlin.math.roundToInt

@Composable
internal fun MainBackground(
    modifier: Modifier = Modifier,
    backgroundMapCode: String?,
) {
    backgroundMapCode?.let {
        val mapResourceCode = MapResourceCode.getResource(code = it)

        Box(modifier = modifier.fillMaxSize()) {
            if (mapResourceCode == MapResourceCode.MP000) {
                /**
                 * MP000 은 지평선 없는 별밭이라 잘라도 티가 나지 않는다.
                 * coil 이 ImageBitmap 이 아니라 painter 로 넘겨주므로 아래 클램프 경로를 못 탄다.
                 */
                Image(
                    painter = rememberAsyncImagePainter(
                        model = R.drawable.map_mp000_gif,
                        imageLoader = LocalMongsImageLoader.current,
                        placeholder = painterResource(mapResourceCode.code),
                    ),
                    contentDescription = "MainBackground",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                MapImage(
                    resId = mapResourceCode.code,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Box(
                modifier = Modifier
                    /**
                     * 스크림은 색상을 draw 단계에서 읽는다.
                     * Modifier.background(색상) 으로 넘기면 컴포지션 단계에서 읽게 되어
                     * 값이 바뀔 때 이 컴포저블 전체가 리컴포지션된다.
                     */
                    .drawBehind {
                        drawRect(color = Color.Black, alpha = MainDimens.ScrimAlpha)
                    }
                    .fillMaxSize()
                    .zIndex(1f)
            )
        }
    } ?: run {
        DefaultBackground()
    }
}

/**
 * 정사각(650x650) 맵을 가로 화면에 세로 기준으로 채우고,
 * 남는 좌우 여백을 가장자리 1픽셀 열로 늘려 메운다.
 *
 * ContentScale.Crop 으로 채우면 배율이 1.3배가 되어 세로 46% 만 남는다.
 * 점포 간판 위와 지면 아래가 잘리는데, 이 앱에서 점포 자체가 수집 대상이라
 * 자르는 쪽이 잘못된 선택이다. 맵의 좌우 가장자리는 하늘 띠와 지면 띠로
 * 수평 균일해서, 그 열을 늘리면 배경이 자연스럽게 이어진다.
 *
 * FilterQuality.None 은 필수다. 650 -> 1080px 는 1.66배 업스케일이라
 * 기본 보간을 쓰면 픽셀 글자와 스프라이트가 뭉갠다.
 */
@Composable
private fun MapImage(
    resId: Int,
    modifier: Modifier = Modifier,
) {
    val bitmap: ImageBitmap = ImageBitmap.imageResource(resId)

    Canvas(modifier = modifier) {
        val dstH = size.height.roundToInt()
        if (dstH <= 0 || bitmap.height <= 0) return@Canvas

        val scale = size.height / bitmap.height
        val dstW = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
        val left = ((size.width - dstW) / 2f).roundToInt().coerceAtLeast(0)
        val right = (size.width.roundToInt() - left - dstW).coerceAtLeast(0)

        if (left > 0) {
            drawImage(
                image = bitmap,
                srcOffset = IntOffset(0, 0),
                srcSize = IntSize(1, bitmap.height),
                dstOffset = IntOffset(0, 0),
                dstSize = IntSize(left, dstH),
                filterQuality = FilterQuality.None,
            )
        }
        if (right > 0) {
            drawImage(
                image = bitmap,
                srcOffset = IntOffset(bitmap.width - 1, 0),
                srcSize = IntSize(1, bitmap.height),
                dstOffset = IntOffset(left + dstW, 0),
                dstSize = IntSize(right, dstH),
                filterQuality = FilterQuality.None,
            )
        }
        drawImage(
            image = bitmap,
            srcOffset = IntOffset(0, 0),
            srcSize = IntSize(bitmap.width, bitmap.height),
            dstOffset = IntOffset(left, 0),
            dstSize = IntSize(dstW, dstH),
            filterQuality = FilterQuality.None,
        )
    }
}

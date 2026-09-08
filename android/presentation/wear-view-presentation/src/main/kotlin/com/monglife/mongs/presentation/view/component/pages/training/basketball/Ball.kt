package com.monglife.mongs.presentation.view.component.pages.training.basketball

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.vo.BallVo
import com.mongs.presentation.view.wear.R

/**
 * 공을 그린다.
 *
 * BallVo 를 값이 아니라 람다로 받는다.
 * 값으로 받으면 물리 틱(16ms)마다 이 컴포저블이 리컴포지션되지만,
 * 람다로 받아 DrawScope 안에서 읽으면 draw 단계만 다시 돈다.
 */
@Composable
internal fun Ball(
    modifier: Modifier = Modifier,
    ballVo: () -> BallVo?,
) {
    val image = ImageBitmap.imageResource(R.drawable.btn_icon_basketball)

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val vo = ballVo() ?: return@Canvas

        drawCircle(
            color = Color.Black,
            radius = vo.radius.minus(2.5f),
            center = Offset(
                x = vo.px,
                y = vo.py
            ),
        )

        rotate(degrees = vo.degree, pivot = Offset(vo.px, vo.py)) {
            drawImage(
                image = image,
                dstSize = IntSize(
                    width = vo.radius.toInt() * 2,
                    height = vo.radius.toInt() * 2,
                ),
                dstOffset = IntOffset(
                    x = (vo.px - vo.radius).toInt(),
                    y = (vo.py - vo.radius).toInt()
                ),
            )
        }
    }
}

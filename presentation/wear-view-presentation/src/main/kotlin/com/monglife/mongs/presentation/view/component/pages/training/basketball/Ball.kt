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

@Composable
internal fun Ball(
    modifier: Modifier = Modifier,
    ballVo: BallVo,
) {
    val image = ImageBitmap.imageResource(R.drawable.btn_icon_basketball)

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawCircle(
            color = Color.Black,
            radius = ballVo.radius.minus(2.5f),
            center = Offset(
                x = ballVo.px,
                y = ballVo.py
            ),
        )

        rotate(degrees = ballVo.degree, pivot = Offset(ballVo.px, ballVo.py)) {
            drawImage(
                image = image,
                dstSize = IntSize(
                    width = ballVo.radius.toInt() * 2,
                    height = ballVo.radius.toInt() * 2,
                ),
                dstOffset = IntOffset(
                    x = (ballVo.px - ballVo.radius).toInt(),
                    y = (ballVo.py - ballVo.radius).toInt()
                ),
            )
        }
    }
}
package com.monglife.mongs.presentation.view.component.pages.training.basketball

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.vo.BasketVo
import com.mongs.presentation.view.wear.R

/**
 * 골대를 그린다.
 *
 * BasketVo 를 값이 아니라 람다로 받는다.
 * 값으로 받으면 물리 틱(16ms)마다 이 컴포저블이 리컴포지션되지만,
 * 람다로 받아 DrawScope 안에서 읽으면 draw 단계만 다시 돈다.
 */
@Composable
internal fun Basket(
    modifier: Modifier = Modifier,
    basketVo: () -> BasketVo?,
) {
    val imageBitmap = ImageBitmap.imageResource(R.drawable.icon_basket)

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val vo = basketVo() ?: return@Canvas
        val width = (vo.width + vo.radius * 2).toInt()
        val height = imageBitmap.height * width / imageBitmap.width

        drawImage(
            image = imageBitmap,
            dstSize = IntSize(
                width = width,
                height = height,
            ),
            dstOffset = IntOffset(
                x = (vo.px - vo.width / 2 - vo.radius).toInt(),
                y = (vo.py - vo.height / 2).toInt(),
            ),
        )
    }
}

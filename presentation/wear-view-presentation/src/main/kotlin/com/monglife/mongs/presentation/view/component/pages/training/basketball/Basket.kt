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

@Composable
internal fun Basket(
    modifier: Modifier = Modifier,
    basketVo: BasketVo,
) {
    val imageBitmap = ImageBitmap.imageResource(R.drawable.icon_basket)
    val width = (basketVo.width + basketVo.radius * 2).toInt()
    val height =  imageBitmap.height * width / imageBitmap.width

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawImage(
            image = imageBitmap,
            dstSize = IntSize(
                width = width,
                height = height,
            ),
            dstOffset = IntOffset(
                x = (basketVo.px - basketVo.width / 2 - basketVo.radius).toInt(),
                y = (basketVo.py - basketVo.height / 2).toInt(),
            ),
        )
    }
}
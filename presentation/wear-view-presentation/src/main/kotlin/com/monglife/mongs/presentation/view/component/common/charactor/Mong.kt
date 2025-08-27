package com.monglife.mongs.presentation.view.component.common.charactor

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.mongs.presentation.view.wear.R

@Composable
internal fun Mong(
    modifier: Modifier = Modifier,
    mong: MongResourceCode,
    state: MongStatusCode = MongStatusCode.NORMAL,
    isHappy: Boolean = false,
    isEating: Boolean = false,
    isSleep: Boolean = false,
    onClick: () -> Unit = {},
    ratio: Float = 1f,
    isPng: Boolean = false,
) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier.zIndex(0f)
        ) {
            if (isPng) {
                Image(
                    painter = painterResource(id = mong.pngCode),
                    contentDescription = "Mong",
                    modifier = Modifier.size((120 * ratio).dp)
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = mong.gifCode,
                        imageLoader = imageLoader
                    ),
                    contentDescription = "Mong",
                    modifier = Modifier.size((120 * ratio).dp)
                )
            }
        }

        if (!isPng && mong.hasExpression) {
            Box(
                modifier = Modifier.zIndex(1f)
            ) {
                val expression = when (state) {
                    MongStatusCode.SICK -> mong.sadGifCode
                    MongStatusCode.SOMNOLENCE -> mong.depressedGifCode
                    MongStatusCode.HUNGRY -> mong.sulkyGifCode
                    else -> {
                        if (isHappy) {
                            mong.happyGifCode
                        } else if (isEating) {
                            mong.eatingGifCode
                        } else if (isSleep) {
                            mong.sleepingGifCode
                        } else {
                            mong.smileGifCode
                        }
                    }
                }

                Image(
                    painter = rememberAsyncImagePainter(model = expression, imageLoader = imageLoader),
                    contentDescription = "MongExpression",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size((35 * ratio).dp)
                        .offset(y = (mong.yOffset * ratio).dp, x = (mong.xOffset * ratio).dp)
                )
            }
        }
    }
}
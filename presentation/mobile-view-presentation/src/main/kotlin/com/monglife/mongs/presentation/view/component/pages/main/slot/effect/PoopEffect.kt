package com.monglife.mongs.presentation.view.component.pages.main.slot.effect

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.mongs.presentation.view.mobile.R
import kotlin.math.min

private val poopPadding = arrayOf(
    PaddingValues(end = 90.dp, bottom = 27.dp),
    PaddingValues(start = 81.dp, bottom = 24.dp),
    PaddingValues(end = 120.dp, bottom = 39.dp),
    PaddingValues(start = 114.dp, bottom = 42.dp),
)

@Composable
internal fun PoopEffect(
    modifier: Modifier = Modifier,
    poopCount: Int = 0,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.fillMaxSize(),
    ) {
        for (count in 1..min(poopCount, poopPadding.size)) {
            Image(
                modifier = Modifier
                    .padding(poopPadding[count - 1])
                    .size(38.dp)
                    .zIndex(-count.toFloat()),
                painter = painterResource(R.drawable.icon_poop),
                contentDescription = null
            )
        }
    }
}
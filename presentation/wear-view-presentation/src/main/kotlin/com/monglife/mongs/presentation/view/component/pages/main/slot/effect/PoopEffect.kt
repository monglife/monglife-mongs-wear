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
import com.mongs.wear.presentation.view.wear.R
import kotlin.math.min

private val poopPadding = arrayOf(
    PaddingValues(end = 60.dp, bottom = 18.dp),
    PaddingValues(start = 54.dp, bottom = 16.dp),
    PaddingValues(end = 80.dp, bottom = 26.dp),
    PaddingValues(start = 76.dp,bottom = 28.dp),
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
                    .size(25.dp)
                    .zIndex(-count.toFloat()),
                painter = painterResource(R.drawable.icon_poop),
                contentDescription = null
            )
        }
    }
}
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
    PaddingValues(end = 75.dp, bottom = 22.dp),
    PaddingValues(start = 68.dp, bottom = 20.dp),
    PaddingValues(end = 100.dp, bottom = 33.dp),
    PaddingValues(start = 95.dp, bottom = 35.dp),
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
                    .size(31.dp)
                    .zIndex(-count.toFloat()),
                painter = painterResource(R.drawable.icon_poop),
                contentDescription = null
            )
        }
    }
}
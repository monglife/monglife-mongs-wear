package com.monglife.mongs.presentation.view.component.common.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mongs.presentation.view.wear.R

@Composable
internal fun RightButton(
    modifier: Modifier = Modifier,
    height: Int = 35,
    width: Int = 18,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height.dp)
            .width(width.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        val buttonImage = R.drawable.btn_icon_right

        Image(
            painter = painterResource(buttonImage),
            contentDescription = "RightButton",
            modifier = Modifier.zIndex(1.1f),
            contentScale = ContentScale.FillBounds
        )
    }
}

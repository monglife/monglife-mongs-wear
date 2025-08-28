package com.monglife.mongs.presentation.view.component.common.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mongs.presentation.view.mobile.R

@Composable
internal fun CircleImageButton(
    modifier: Modifier = Modifier,
    icon: Int,
    border: Int,
    size: Int = 54,
    iconSize: Float = size / 2f,
    disable: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .background(
                color = Color.Transparent,
                shape = MaterialTheme.shapes.large
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!disable) {
                        onClick()
                    }
                }
            )
    ) {
        Image(
            alpha = 0.55f,
            painter = painterResource(R.drawable.btn_bg_circle),
            contentDescription = null,
            modifier = Modifier.zIndex(0f)
        )

        Image(
            alpha = if (disable) 0.4f else 1f,
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize.dp)
                .zIndex(if (disable) -1f else 1f)
        )

        if (disable) {
            Image(
                painter = painterResource(R.drawable.btn_icon_locker),
                contentDescription = null,
                modifier = Modifier
                    .size((size / 2).dp)
                    .zIndex(2f)
            )
        }

        Image(
            painter = painterResource(border),
            contentDescription = null,
            modifier = Modifier.zIndex(3f)
        )
    }
}


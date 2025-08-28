package com.monglife.mongs.presentation.view.component.common.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mongs.presentation.view.mobile.R

@Composable
internal fun GoogleSignInButton(
    onClick: () -> Unit = {},
    height: Int = 90,
    width: Int = 320,
) {
    Image(
        painter = painterResource(R.drawable.btn_google_login),
        contentDescription = "GoogleSignInButton",
        modifier = Modifier
            .height(height.dp)
            .width(width.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentScale = ContentScale.FillBounds,
    )
}
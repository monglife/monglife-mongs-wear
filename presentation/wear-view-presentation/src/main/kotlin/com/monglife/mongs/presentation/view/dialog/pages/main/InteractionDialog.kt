package com.monglife.mongs.presentation.view.dialog.pages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.component.common.textbox.PayPoint
import com.monglife.mongs.presentation.wear.component.common.button.CircleImageButton
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun InteractionDialog(
    modifier: Modifier = Modifier,
    payPoint: Int,
    level: Int,
    stateCode: MongStateCode,
    isSleep: Boolean,
    onInventoryClick: () -> Unit,
    onFeedClick: () -> Unit,
    onSleepClick: () -> Unit,
    onPoopCleanClick: () -> Unit,
    onStrokeClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Black.copy(alpha = 0.85f))
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCloseClick,
            )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .weight(0.4f),
            ) {
                PayPoint(
                    payPoint = payPoint,
                    width = 100,
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .weight(0.3f)
            ) {
                CircleImageButton(
                    icon = R.drawable.btn_icon_sleep,
                    border = R.drawable.btn_border_blue,
                    iconSize = 34f,
                    disable = level == 0 || stateCode == MongStateCode.DEAD,
                    onClick = onSleepClick,
                )

                Spacer(modifier = Modifier.width(8.dp))

                CircleImageButton(
                    icon = R.drawable.btn_icon_stroke,
                    border = R.drawable.btn_border_pink,
                    iconSize = 34f,
                    disable = level == 0 || stateCode == MongStateCode.DEAD || isSleep,
                    onClick = onStrokeClick,
                )

                Spacer(modifier = Modifier.width(8.dp))

                CircleImageButton(
                    icon = R.drawable.btn_icon_poop_clean,
                    border = R.drawable.btn_border_purple,
                    iconSize = 34f,
                    disable = level == 0 || stateCode == MongStateCode.DEAD || isSleep,
                    onClick = onPoopCleanClick,
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .weight(0.3f)
            ) {
                CircleImageButton(
                    icon = R.drawable.btn_icon_feed,
                    border = R.drawable.btn_border_yellow,
                    iconSize = 34f,
                    disable = level == 0 || stateCode == MongStateCode.DEAD || isSleep,
                    onClick = onFeedClick,
                )

                Spacer(modifier = Modifier.width(8.dp))

                CircleImageButton(
                    icon = R.drawable.btn_icon_inventory,
                    border = R.drawable.btn_border_green,
                    iconSize = 34f,
                    onClick = onInventoryClick,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
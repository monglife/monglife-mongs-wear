package com.monglife.mongs.presentation.view.dialog.pages.help

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton

@Composable
internal fun HelpDialog(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    contents: List<String>,
    cancel: () -> Unit,
) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 1)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = Color.Black.copy(alpha = 0.95f))
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = cancel,
            )
    ) {
        PositionIndicator(scalingLazyListState = listState)
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 60.dp, horizontal = 6.dp),
            state = listState,
            autoCentering = null,
            verticalArrangement = Arrangement.spacedBy(space = 12.dp, alignment = Alignment.Top),
        ) {
            icon?.let {
                item {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .height(25.dp)
                            .width(25.dp),
                        contentScale = ContentScale.FillBounds,
                    )
                }
            }

            for (content in contents) {
                item {
                    if (content == "\n") {
                        Spacer(modifier = Modifier.height(5.dp))
                    } else {
                        Text(
                            text = content,
                            textAlign = TextAlign.Center,
                            fontFamily = DAL_MU_RI,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            color = MongsWhite,
                            maxLines = 1,
                        )
                    }
                }
            }

            item {
                BlueButton(
                    text = "닫기",
                    onClick = cancel,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
package com.monglife.mongs.presentation.view.dialog.pages.feedback

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.textbox.InputTextBox


@Composable
internal fun CreateFeedbackDialog(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    confirm: () -> Unit,
    cancel: () -> Unit,
    changeText: (String) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .zIndex(1f)
            .background(color = Color.Black.copy(alpha = 0.95f))
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = cancel,
            )
    ) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
            ) {
                Text(
                    text = "오류 메시지",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = MongsWhite,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                InputTextBox(
                    text = content,
                    maxLines = 4,
                    textAlign = TextAlign.Start,
                    changeInput = changeText,
                    icon = false,
                    placeholder = "$title 오류 발생",
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(144.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
            ) {
                BlueButton(
                    text = "닫기",
                    onClick = cancel,
                )

                Spacer(modifier = Modifier.width(10.dp))

                BlueButton(
                    text = "확인",
                    disable = content.isEmpty(),
                    onClick = confirm,
                )
            }
        }
    }
}
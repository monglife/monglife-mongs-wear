package com.monglife.mongs.presentation.view.dialog.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton

/**
 * 확인 / 취소 다이얼로그.
 *
 * wear 의 줄 분리(`text.trim().split("\n")`) 계약을 그대로 유지한다 - 호출부가
 * 워치 폭에 맞춰 줄바꿈을 넣어 둔 문자열을 넘긴다.
 *
 * 바깥을 눌러도 닫히는 건 wear 와 같다. 880dp 폭에서는 실수로 누를 면적이 커지지만,
 * 모달 스크림에서 그건 폰에서도 기대되는 동작이다.
 */
@Composable
internal fun ConfirmAndCancelDialog(
    modifier: Modifier = Modifier,
    text: String,
    confirm: () -> Unit,
    cancel: () -> Unit,
) {
    val texts = remember(text) { text.trim().split("\n") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = cancel,
            )
    ) {
        Column(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            texts.forEach {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    color = MongsWhite,
                    maxLines = 1,
                )
            }

            Row(
                modifier = Modifier.padding(top = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BlueButton(text = "닫기", width = 110, height = 44, fontSize = 15, onClick = cancel)
                BlueButton(text = "확인", width = 110, height = 44, fontSize = 15, onClick = confirm)
            }
        }
    }
}

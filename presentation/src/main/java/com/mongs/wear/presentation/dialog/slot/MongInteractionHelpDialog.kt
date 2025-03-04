package com.mongs.wear.presentation.dialog.slot

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.wear.compose.material.Text
import com.mongs.wear.domain.management.vo.MongVo
import com.mongs.wear.presentation.R
import com.mongs.wear.presentation.assets.DAL_MU_RI
import com.mongs.wear.presentation.assets.MongsWhite
import com.mongs.wear.presentation.component.common.button.BlueButton

@Composable
fun MongInteractionHelpDialog(
    mongVo: MongVo,
    close: () -> Unit,
    closeEver: () -> Unit,
    modifier: Modifier = Modifier.zIndex(0f),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Black.copy(alpha = 0.85f))
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = close,
            )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .weight(0.35f)
            ) {
                Image(
                    painter = painterResource(R.drawable.btn_icon_notice),
                    contentDescription = null,
                    modifier = Modifier
                        .height(35.dp)
                        .width(35.dp),
                    contentScale = ContentScale.FillBounds,
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .weight(0.2f)
            ) {
                Text(
                    text = "몽을 클릭하면",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = MongsWhite,
                    maxLines = 1,
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .weight(0.2f)
            ) {
                Text(
                    text = "상호작용 메뉴가 열려요",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = MongsWhite,
                    maxLines = 1,
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .weight(0.25f)
            ) {
                BlueButton(
                    text = "닫기",
                    onClick = close,
                )

                Spacer(modifier = Modifier.width(5.dp))

                BlueButton(
                    text = "그만보기",
                    width = 78,
                    onClick = closeEver
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
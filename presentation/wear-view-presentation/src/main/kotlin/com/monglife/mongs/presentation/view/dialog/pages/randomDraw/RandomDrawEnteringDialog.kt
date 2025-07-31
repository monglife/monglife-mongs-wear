package com.monglife.mongs.presentation.view.dialog.pages.randomDraw

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.textbox.PayPointBox
import com.monglife.mongs.presentation.view.component.common.textbox.TicketBox
import com.mongs.wear.presentation.view.wear.R

@Composable
fun RandomDrawEnteringDialog(
    modifier: Modifier = Modifier,
    randomDrawPayPoint: Int,
    payPoint: Int,
    randomDrawTicketCount: Int,
    onDrawClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = Color.Black.copy(alpha = 0.75f))
            .fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
            ) {
                if (randomDrawTicketCount > 0) {
                    TicketBox(ticket = randomDrawTicketCount)
                } else {
                    PayPointBox(payPoint = payPoint)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
            ) {
                Text(
                    text = "랜덤뽑기",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp,
                    color = MongsWhite,
                    maxLines = 1,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                if (randomDrawTicketCount > 0) {
                    Image(
                        painter = painterResource(R.drawable.icon_ticket),
                        contentDescription = null,
                        modifier = Modifier
                            .height(24.dp)
                            .width(24.dp),
                        contentScale = ContentScale.FillBounds,
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "- 1",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.point_icon_pay),
                        contentDescription = null,
                        modifier = Modifier
                            .height(20.dp)
                            .width(20.dp),
                        contentScale = ContentScale.FillBounds,
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "- $randomDrawPayPoint",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
            ) {
                BlueButton(
                    text = "뽑기",
                    disable = randomDrawPayPoint > payPoint && randomDrawTicketCount <= 0,
                    onClick = onDrawClick,
                )
            }

            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}
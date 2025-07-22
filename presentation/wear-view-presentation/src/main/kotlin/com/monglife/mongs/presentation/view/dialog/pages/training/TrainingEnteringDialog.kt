package com.monglife.mongs.presentation.view.dialog.pages.training

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
import com.monglife.mongs.application.mong.vo.TrainingTypeVo
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsPink200
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.mongs.wear.presentation.view.wear.R

@Composable
fun TrainingEnteringDialog(
    modifier: Modifier = Modifier,
    trainingTypeVo: TrainingTypeVo?,
    onClick: () -> Unit,
) {
    trainingTypeVo?.let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .background(color = Color.Black.copy(alpha = 0.85f))
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        ) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                ) {
                    Text(
                        text = "Mongs",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 18.sp,
                        color = MongsPink200,
                        maxLines = 1,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                ) {
                    Text(
                        text = it.trainingName,
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 28.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.15f)
                ) {
                    Text(
                        text = "제한시간 " + if(it.timeout > 0) "${it.timeout}s" else "없음",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 13.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.15f)
                ) {
                    Text(
                        text = "터치해서 시작하기",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 13.sp,
                        color = MongsPink200,
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
                        text = "+ ${it.payPoint}",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
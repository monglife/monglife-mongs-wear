package com.monglife.mongs.presentation.view.dialog.pages.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.mongs.wear.presentation.view.wear.R

@Composable
fun FeedItemDetailDialog(
    modifier: Modifier = Modifier,
    weight: Double = 0.0,
    strength: Double = 0.0,
    satiety: Double = 0.0,
    healthy: Double = 0.0,
    fatigue: Double = 0.0,
    onClick: () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = Color.Black.copy(alpha = 0.9f))
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(35.dp))
            if (healthy > 0) {
                ImageDetail(
                    image = R.drawable.icon_healthy,
                    value = healthy,
                    modifier = Modifier.weight(0.2f)
                )
            }
            if (satiety > 0) {
                ImageDetail(
                    image = R.drawable.icon_satiety,
                    value = satiety,
                    modifier = Modifier.weight(0.2f)
                )
            }
            if (strength > 0) {
                ImageDetail(
                    image = R.drawable.icon_strength,
                    value = strength,
                    modifier = Modifier.weight(0.2f)
                )
            }
            if (fatigue > 0) {
                ImageDetail(
                    image = R.drawable.icon_fatigue,
                    value = fatigue,
                    modifier = Modifier.weight(0.2f)
                )
            }
            if (weight > 0) {
                TextDetail(text = "Kg", value = weight, modifier = Modifier.weight(0.2f))
            }
            Spacer(modifier = Modifier.height(35.dp))
        }
    }
}

@Composable
private fun ImageDetail(
    modifier: Modifier = Modifier,
    image: Int,
    value: Double,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.width(100.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(0.2f),
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp),
                contentScale = ContentScale.FillBounds
            )
        }

        Text(
            text = "+ $value",
            textAlign = TextAlign.Center,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 20.sp,
            color = MongsWhite,
            maxLines = 1,
            modifier = Modifier.weight(0.8f)
        )
    }
}


@Composable
private fun TextDetail(
    modifier: Modifier = Modifier,
    text: String,
    value: Double,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.width(100.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(0.2f),
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 18.sp,
                color = MongsWhite,
                maxLines = 1,
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp),
            )
        }

        Text(
            text = "+ $value",
            textAlign = TextAlign.Center,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 20.sp,
            color = MongsWhite,
            maxLines = 1,
            modifier = Modifier.weight(0.8f),
        )
    }
}
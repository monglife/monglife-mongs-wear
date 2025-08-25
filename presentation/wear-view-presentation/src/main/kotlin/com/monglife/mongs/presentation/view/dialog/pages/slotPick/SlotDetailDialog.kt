package com.monglife.mongs.presentation.view.dialog.pages.slotPick

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsBlue
import com.monglife.mongs.presentation.view.assets.MongsGreen
import com.monglife.mongs.presentation.view.assets.MongsLightGray
import com.monglife.mongs.presentation.view.assets.MongsPink
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.component.common.textbox.PayPointBox
import com.mongs.presentation.view.wear.R
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val SLOT_DETAIL_ROUND_SIZE = 10
private const val SLOT_DETAIL_WIDTH = 144
private const val SLOT_DETAIL_HEIGHT = 130
private const val SLOT_DETAIL_BAR_HEIGHT = 32

@Composable
internal fun SlotDetailDialog(
    modifier: Modifier = Modifier,
    initTabIndex: Int = 0,
    mongId: Long = 0L,
    statusCode: MongStatusCode,
    stateCode: MongStateCode,
    isSleep: Boolean,
    weight: Double = 0.0,
    healthyRatio: Double = 0.0,
    satietyRatio: Double = 0.0,
    strengthRatio: Double = 0.0,
    fatigueRatio: Double = 0.0,
    payPoint: Int = 0,
    born: LocalDateTime = LocalDateTime.now().minusDays(1).plusSeconds(1),
    onClick: () -> Unit = {},
) {
    val tabIndex = remember{ mutableIntStateOf(initTabIndex) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = Color.Black.copy(alpha = 0.95f))
            .fillMaxSize()
            .zIndex(0f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(width = SLOT_DETAIL_WIDTH.dp, height = SLOT_DETAIL_HEIGHT.dp)
                    .zIndex(1f)
                    .clip(RoundedCornerShape(SLOT_DETAIL_ROUND_SIZE.dp))
                    .background(color = Color.LightGray)
                    .align(Alignment.BottomCenter)
            )

            Box(
                modifier = Modifier
                    .size(width = SLOT_DETAIL_WIDTH.dp, height = SLOT_DETAIL_BAR_HEIGHT.dp)
                    .zIndex(2f)
                    .align(Alignment.TopCenter)
            ) {
                Row (
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    SlotDetailTab(
                        text = "정보",
                        color = if (tabIndex.intValue == 0) MongsLightGray else Color.LightGray,
                        onClick = { tabIndex.intValue = 0 },
                    )
                    SlotDetailTab(
                        text = "지수",
                        color = if (tabIndex.intValue == 1) MongsLightGray else Color.LightGray,
                        onClick = { tabIndex.intValue = 1 },
                    )
                    SlotDetailTab(
                        text = "상태",
                        color = if (tabIndex.intValue == 2) MongsLightGray else Color.LightGray,
                        onClick = { tabIndex.intValue = 2 },
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            bottomStart = SLOT_DETAIL_ROUND_SIZE.dp,
                            bottomEnd = SLOT_DETAIL_ROUND_SIZE.dp
                        )
                    )
                    .background(color = MongsLightGray)
                    .size(width = SLOT_DETAIL_WIDTH.dp, height = (SLOT_DETAIL_HEIGHT - SLOT_DETAIL_BAR_HEIGHT).dp)
                    .zIndex(2f)
                    .align(Alignment.BottomCenter)
            ) {
                when (tabIndex.intValue) {
                    0 -> InfoTabContent(
                        modifier = Modifier.matchParentSize(),
                        mongId = mongId,
                        payPoint = payPoint,
                        born = born,
                        weight = weight,
                    )
                    1 -> StatusTabContent(
                        modifier = Modifier.matchParentSize(),
                        healthy = healthyRatio,
                        satiety = satietyRatio,
                        strength = strengthRatio,
                        sleep = fatigueRatio,
                    )
                    2 -> StateTabContent(
                        modifier = Modifier.matchParentSize(),
                        statusCode = statusCode,
                        stateCode = stateCode,
                        isSleep = isSleep,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoTabContent(
    modifier: Modifier = Modifier,
    mongId: Long,
    payPoint: Int,
    born: LocalDateTime,
    weight: Double,
) {
    val age = remember { mutableStateOf("00시간 00분 00초") }
    LaunchedEffect(mongId) {
        while(true) {
            val now = LocalDateTime.now()
            val hours = ChronoUnit.HOURS.between(born, now)
            val minutes = ChronoUnit.MINUTES.between(born.plusHours(hours), now)
            val seconds = ChronoUnit.SECONDS.between(born.plusHours(hours).plusMinutes(minutes), now)
            age.value = "%02d시간 %02d분 %02d초".format(hours, minutes, seconds)
            delay(1000)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(8.dp)
    ) {
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(SLOT_DETAIL_WIDTH.dp)
                .weight(0.2f)
        ) {
            PayPointBox(
                width = 110,
                payPoint = payPoint,
            )
        }
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(SLOT_DETAIL_WIDTH.dp)
                .weight(0.2f)
        ) {
            Text(
                text = "%.2f kg".format(weight),
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                maxLines = 1,
                color = Color.Black,
            )
        }
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(SLOT_DETAIL_WIDTH.dp)
                .weight(0.2f)
        ) {
            Text(
                text = age.value,
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                maxLines = 1,
                color = Color.Black,
            )
        }
    }
}

@Composable
private fun StatusTabContent(
    modifier: Modifier = Modifier,
    healthy: Double,
    satiety: Double,
    strength: Double,
    sleep: Double,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Row {
            SlotDetailCondition(
                icon = R.drawable.icon_healthy,
                progress = healthy.toFloat(),
                indicatorColor = MongsPink
            )
            SlotDetailCondition(
                icon = R.drawable.icon_satiety,
                progress = satiety.toFloat(),
                indicatorColor = MongsYellow
            )
        }
        Row{
            SlotDetailCondition(
                icon = R.drawable.icon_strength,
                progress = strength.toFloat(),
                indicatorColor = MongsGreen
            )
            SlotDetailCondition(
                icon = R.drawable.icon_fatigue,
                progress = sleep.toFloat(),
                indicatorColor = MongsBlue
            )
        }
    }
}

@Composable
private fun StateTabContent(
    modifier: Modifier = Modifier,
    statusCode: MongStatusCode,
    stateCode: MongStateCode,
    isSleep: Boolean,
) {
    Column(
        modifier = modifier.padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(SLOT_DETAIL_WIDTH.dp)
                .weight(0.3f)
        ) {
            Text(
                text = if(isSleep) "수면중" else "기상",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                maxLines = 1,
                color = Color.Black,
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(SLOT_DETAIL_WIDTH.dp)
                .weight(0.3f)
        ) {
            Text(
                text = statusCode.message,
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                maxLines = 1,
                color = Color.Black,
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(SLOT_DETAIL_WIDTH.dp)
                .weight(0.3f)
        ) {
            Text(
                text = stateCode.message,
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                maxLines = 1,
                color = Color.Black,
            )
        }
    }
}

@Composable
private fun SlotDetailCondition(
    icon: Int,
    progress: Float,
    indicatorColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(5.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            trackColor = Color.LightGray,
            progress = progress / 100,
            strokeWidth = (3.5).dp,
            indicatorColor = indicatorColor,
        )
    }
}

@Composable
private fun SlotDetailTab(
    onClick: () -> Unit,
    color: Color,
    width: Int = 48,
    text: String,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = SLOT_DETAIL_ROUND_SIZE.dp, topEnd = SLOT_DETAIL_ROUND_SIZE.dp))
            .size(width = width.dp, height = SLOT_DETAIL_BAR_HEIGHT.dp)
            .background(color = color)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp,
            color = Color.Black,
        )
    }
}
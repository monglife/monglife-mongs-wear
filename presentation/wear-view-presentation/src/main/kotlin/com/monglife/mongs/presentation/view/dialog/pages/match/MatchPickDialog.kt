package com.monglife.mongs.presentation.view.dialog.pages.match

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
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
import com.monglife.mongs.presentation.view.assets.MongsLightGray
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.bar.ProgressIndicator
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun MatchPickDialog(
    modifier: Modifier = Modifier,
    maxSeconds: Int,
    onAttackClick: () -> Unit,
    onDefenceClick: () -> Unit,
    onHealClick: () -> Unit,
) {
    val progress = remember { mutableFloatStateOf(0f) }
    val timer = remember { mutableFloatStateOf(0f) }

    // 타이머
    LaunchedEffect(Unit) {
        while (progress.floatValue < 100f) {
            delay(200)
            timer.floatValue += 0.2f
            progress.floatValue = timer.floatValue / maxSeconds.toFloat() * 100f
        }
    }

    LaunchedEffect(progress.floatValue) {
        if (progress.floatValue >= 100f) {
            when (Random.nextInt(3)) {
                0 -> { onAttackClick() }
                1 -> { onDefenceClick() }
                else -> { onHealClick() }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = Color.Black.copy(alpha = 0.95f))
            .fillMaxSize()
    ) {
        ProgressIndicator(
            progress = progress.floatValue,
            modifier = Modifier.zIndex(2f)
        )

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.zIndex(1f)
                .fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(0.51f)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(0.49f)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 25.dp, end = 15.dp)
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onHealClick,
                            ),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Text(
                            text = "회복",
                            textAlign = TextAlign.Center,
                            fontFamily = DAL_MU_RI,
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            color = MongsWhite,
                            maxLines = 1,
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(0.02f)
                ) {
                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .background(MongsLightGray)
                        .width(2.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(0.49f)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 25.dp, start = 15.dp)
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDefenceClick,
                            ),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = "방어",
                            textAlign = TextAlign.Center,
                            fontFamily = DAL_MU_RI,
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            color = MongsWhite,
                            maxLines = 1,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.weight(0.02f)
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(MongsLightGray)
                    .height(2.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(0.47f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 25.dp)
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAttackClick,
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "공격",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 20.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
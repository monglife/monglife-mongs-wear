package com.monglife.mongs.presentation.view.component.pages.training.rockPaperScissors.section

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.TrainingRockPaperScissorsViewModel
import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.enums.RockPaperScissorsPickCode
import com.mongs.presentation.view.wear.R
import kotlinx.coroutines.delay

@Composable
internal fun RockPaperScissorsSection(
    modifier: Modifier = Modifier,
    trainingRockPaperScissorsViewModel: TrainingRockPaperScissorsViewModel,
) {
    val rockPaperScissorsPickCode = remember { mutableStateOf<RockPaperScissorsPickCode?>(null) }
    val showResult = remember { mutableStateOf(false) }
    val rockPaperScissorsVo = trainingRockPaperScissorsViewModel.rockPaperScissorsVo.collectAsState()

    LaunchedEffect(Unit) {
        showResult.value = false

        for (pickCode in RockPaperScissorsPickCode.entries) {
            rockPaperScissorsPickCode.value = pickCode
            delay(500)
        }

        trainingRockPaperScissorsViewModel.over()
        showResult.value = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        if (!showResult.value) {
            rockPaperScissorsPickCode.value?.let {
                Image(
                    painter = painterResource(
                        when (it) {
                            RockPaperScissorsPickCode.ROCK -> R.drawable.icon_rock
                            RockPaperScissorsPickCode.PAPER -> R.drawable.icon_paper
                            RockPaperScissorsPickCode.SCISSORS -> R.drawable.icon_scissors
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .height(70.dp)
                        .width(70.dp),
                    contentScale = ContentScale.FillBounds
                )
            }
        } else {
            rockPaperScissorsVo.value?.let {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.425f)
                    ) {
                        it.randomPickCode?.let { randomPickCode ->
                            Image(
                                painter = painterResource(
                                    when (randomPickCode) {
                                        RockPaperScissorsPickCode.ROCK -> R.drawable.icon_rock
                                        RockPaperScissorsPickCode.PAPER -> R.drawable.icon_paper
                                        RockPaperScissorsPickCode.SCISSORS -> R.drawable.icon_scissors
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(40.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }

                        Spacer(modifier = Modifier.width(35.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.15f)
                    ) {
                        if (it.result == 0) {
                            Text(
                                text = "무승부",
                                textAlign = TextAlign.Center,
                                fontFamily = DAL_MU_RI,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MongsWhite,
                                maxLines = 1,
                            )
                        } else {
                            Image(
                                painter = painterResource(
                                    if (it.result == 1) R.drawable.txt_win else R.drawable.txt_lose
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(75.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.425f)
                    ) {
                        Spacer(modifier = Modifier.width(35.dp))

                        it.pickCode?.let { pickCode ->
                            Image(
                                painter = painterResource(
                                    when (pickCode) {
                                        RockPaperScissorsPickCode.ROCK -> R.drawable.icon_rock
                                        RockPaperScissorsPickCode.PAPER -> R.drawable.icon_paper
                                        RockPaperScissorsPickCode.SCISSORS -> R.drawable.icon_scissors
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(40.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }

                        Spacer(modifier = Modifier.width(15.dp))

                        Image(
                            painter = painterResource(R.drawable.txt_me),
                            contentDescription = null,
                            modifier = Modifier
                                .height(20.dp)
                                .width(40.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }
    }
}
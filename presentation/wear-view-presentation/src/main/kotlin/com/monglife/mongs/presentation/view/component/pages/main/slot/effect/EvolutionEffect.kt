package com.monglife.mongs.presentation.view.component.pages.main.slot.effect

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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.charactor.Mong
import com.mongs.wear.presentation.view.wear.R
import kotlinx.coroutines.delay

private val EVOLUTION_EFFECTS = listOf(R.drawable.effect_evolution_1, R.drawable.effect_evolution_2, R.drawable.effect_evolution_3)
private val DELAYS = listOf(100L, 300L, 300L, 400L)

@Composable
internal fun EvolutionEffect(
    modifier: Modifier = Modifier,
    mongCode: String,
    isEvolving: Boolean,
    onClick: () -> Unit,
    callback: () -> Unit,
) {
    if (isEvolving) {
        var nowEffectIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            for (effectIndex in 0..2) {
                nowEffectIndex = effectIndex
                delay(DELAYS[effectIndex])
            }
            callback()
        }

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Mong(
                mong = MongResourceCode.getResource(mongCode),
                modifier = Modifier.padding(bottom = 25.dp),
                isPng = true,
            )

            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(EVOLUTION_EFFECTS[nowEffectIndex]),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "진화를 위해",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                    )

                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "화면을 터치해주세요.",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                    )
                }
            }
        }
    }
}

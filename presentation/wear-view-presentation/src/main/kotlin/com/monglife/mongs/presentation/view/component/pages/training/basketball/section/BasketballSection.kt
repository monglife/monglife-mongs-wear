package com.monglife.mongs.presentation.view.component.pages.training.basketball.section

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.monglife.mongs.presentation.view.component.pages.training.basketball.Ball
import com.monglife.mongs.presentation.view.component.pages.training.basketball.Basket
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.TrainingBasketballViewModel

@Composable
fun BasketballSection(
    modifier: Modifier = Modifier,
    trainingBasketballViewModel: TrainingBasketballViewModel,
) {
    val basketballVo = trainingBasketballViewModel.basketballVo.collectAsState()
    val dragOffset = remember { mutableStateOf(Offset(0f, 0f)) }

    basketballVo.value?.let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            change.consume()
                            dragOffset.value = change.position
                        },
                        onDragEnd = {
                            trainingBasketballViewModel.throwBall(
                                basketballId = it.basketballId,
                                vy = dragOffset.value.y,
                                vx = dragOffset.value.x,
                            )
                        }
                    )
                }
        ) {
            Ball(
                modifier = Modifier.zIndex(0f),
                ballVo = it.ballVo,
            )

            Basket(
                modifier = Modifier.zIndex(
                    if (it.ballVo.isTop) 1f
                    else -1f
                ),
                basketVo = it.basketVo,
            )
        }
    }
}
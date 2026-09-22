package com.monglife.mongs.presentation.view.component.pages.training.basketball.section

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
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
    val basketballVo = trainingBasketballViewModel.basketballVo.collectAsStateWithLifecycle()
    val dragOffset = remember { mutableStateOf(Offset(0f, 0f)) }

    /**
     * 엔진이 16ms 마다 새 VO 를 emit 하므로 VO 자체를 컴포지션에서 읽으면
     * 이 섹션 전체가 초당 62.5회 리컴포지션된다.
     * 컴포지션에서는 "표시 여부", "공이 골대 앞인지" 같은 저빈도 값만 읽고,
     * 좌표는 Ball/Basket 의 draw 단계로 내려 보낸다.
     */
    val isVisible = remember(basketballVo) {
        derivedStateOf { basketballVo.value != null }
    }
    val basketballId = remember(basketballVo) {
        derivedStateOf { basketballVo.value?.basketballId }
    }
    val isBallTop = remember(basketballVo) {
        derivedStateOf { basketballVo.value?.ballVo?.isTop == true }
    }

    if (!isVisible.value) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            // 키를 Unit 으로 두면 첫 컴포지션의 basketballId 가 그대로 고정된다.
            .pointerInput(basketballId.value) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        dragOffset.value = change.position
                    },
                    onDragEnd = {
                        basketballVo.value?.let { vo ->
                            trainingBasketballViewModel.throwBall(
                                basketballId = vo.basketballId,
                                vy = dragOffset.value.y,
                                vx = dragOffset.value.x,
                            )
                        }
                    }
                )
            }
    ) {
        Ball(
            modifier = Modifier.zIndex(0f),
            ballVo = { basketballVo.value?.ballVo },
        )

        Basket(
            modifier = Modifier.zIndex(if (isBallTop.value) 1f else -1f),
            basketVo = { basketballVo.value?.basketVo },
        )
    }
}

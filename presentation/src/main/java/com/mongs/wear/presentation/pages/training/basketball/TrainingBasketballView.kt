package com.mongs.wear.presentation.pages.training.basketball

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mongs.wear.core.enums.TrainingCode
import com.mongs.wear.presentation.R
import com.mongs.wear.presentation.assets.MongsDarkPurple
import com.mongs.wear.presentation.assets.NavItem
import com.mongs.wear.presentation.component.background.TrainingNestedBackground
import com.mongs.wear.presentation.component.common.bar.LoadingBar
import com.mongs.wear.presentation.component.common.bar.ProgressIndicator
import com.mongs.wear.presentation.component.common.textbox.ScoreBox
import com.mongs.wear.presentation.dialog.training.TrainingEndDialog
import com.mongs.wear.presentation.dialog.training.TrainingStartDialog

@Composable
fun TrainingBasketballView(
    navController: NavController,
    trainingBasketballViewModel: TrainingBasketballViewModel = hiltViewModel(),
    configuration: Configuration = LocalConfiguration.current,
) {
    val mongVo = trainingBasketballViewModel.mongVo.observeAsState()
    val trainingPayPoint = trainingBasketballViewModel.trainingPayPoint.observeAsState(0)
    val trainingScore = trainingBasketballViewModel.trainingScore.observeAsState(0)
    val time = trainingBasketballViewModel.time.observeAsState(0)
    val timeout = trainingBasketballViewModel.timeout.observeAsState(0)
    val isSuccess = trainingBasketballViewModel.isSuccess.observeAsState(false)

    val score = remember { trainingBasketballViewModel.basketballEngine.score }
    val ball = remember { trainingBasketballViewModel.basketballEngine.ball }
    val basket = remember { trainingBasketballViewModel.basketballEngine.basket }

    Box {
        TrainingNestedBackground()

        if (trainingBasketballViewModel.uiState.loadingBar) {
            TrainingBasketballLoadingBar()
        } else {
            mongVo.value?.let { mongVo ->
                TrainingBasketballContent(
                    ball = ball.value,
                    basket = basket.value,
                    modifier = Modifier.zIndex(1f)
                )

                TrainingBasketballScoreContent(
                    score = score.value,
                    trainingScore = trainingScore.value,
                    modifier = Modifier.zIndex(2f),
                )

                ProgressIndicator(
                    progress = time.value.toFloat() / timeout.value.toFloat() * 100,
                    indicatorColor = MongsDarkPurple,
                    modifier = Modifier.zIndex(3f)
                )

                // 스코어 달성 이벤트
                LaunchedEffect(score.value) {
                    if (score.value >= trainingScore.value) {
                        trainingBasketballViewModel.basketballEnd(
                            mongId = mongVo.mongId,
                            score = score.value,
                        )
                    }
                }

                // 게임 종료 이벤트
                LaunchedEffect(trainingBasketballViewModel.basketballEngine.endEvent) {
                    trainingBasketballViewModel.basketballEngine.endEvent.collect {
                        trainingBasketballViewModel.basketballEnd(
                            mongId = mongVo.mongId,
                            score = score.value,
                        )
                    }
                }
            }

            /**
             * Dialog
             */
            if (trainingBasketballViewModel.uiState.trainingStartDialog) {
                TrainingStartDialog(
                    firstText = "드래그해",
                    secondText = "농구공 넣기",
                    timeout = timeout.value,
                    rewardPayPoint = trainingPayPoint.value,
                    trainingScore = trainingScore.value,
                    trainingStart = {
                        trainingBasketballViewModel.basketballStart(
                            timeout = timeout.value,
                            ballSx = configuration.screenWidthDp.toFloat(),
                            ballSy = configuration.screenHeightDp.toFloat() * 1.73f,
                            basketSx = configuration.screenWidthDp.toFloat(),
                            basketSy = configuration.screenHeightDp.toFloat() * 0.73f,
                        )
                    },
                    modifier = Modifier.zIndex(4f),
                )
            } else if (trainingBasketballViewModel.uiState.trainingOverDialog) {
                TrainingEndDialog(
                    isSuccess = isSuccess.value,
                    rewardPayPoint = trainingPayPoint.value,
                    trainingCode = TrainingCode.BASKETBALL,
                    trainingEnd = { navController.popBackStack(route = NavItem.TrainingBasketball.route, inclusive = true) },
                    modifier = Modifier.zIndex(4f),
                )
            }
        }
    }

    LaunchedEffect(trainingBasketballViewModel.uiState.navMainEvent) {
        trainingBasketballViewModel.uiState.navMainEvent.collect {
            navController.popBackStack(route = NavItem.TrainingBasketball.route, inclusive = true)
        }
    }

    // 프레임 변경 시마다 재 랜더링
    LaunchedEffect(trainingBasketballViewModel.basketballEngine.playMillis.value) {}
}

@Composable
private fun TrainingBasketballContent(
    ball: BasketballEngine.Ball?,
    basket: BasketballEngine.Basket?,
    modifier: Modifier = Modifier.zIndex(0f)
) {
    val dragOffset = remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        dragOffset.value = change.position
                    },
                    onDragEnd = {
                        ball?.throwing(dragOffset.value.y, dragOffset.value.x)
                    }
                )
            }
    ) {
        ball?.let {
            Ball(
                ball = ball,
                modifier = Modifier.zIndex(0f)
            )
        }

        basket?.let {
            Basket(
                basket = basket,
                modifier = Modifier.zIndex(ball?.let { if (ball.isTop.value) 1f else -1f } ?: 1f)
            )
        }
    }
}

@Composable
private fun TrainingBasketballScoreContent(
    score: Int,
    trainingScore: Int,
    modifier: Modifier = Modifier.zIndex(0f),
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 14.dp)
    ) {
        ScoreBox(
            score = score,
            maxScore = trainingScore,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun Ball(
    ball: BasketballEngine.Ball,
    modifier: Modifier = Modifier.zIndex(0f)
) {
    val image = ImageBitmap.imageResource(R.drawable.btn_icon_basketball)

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val radius = ball.radius.value
        val degrees = ball.degrees
        val centerX = ball.currentX.value
        val centerY = ball.currentY.value

        drawCircle(
            color = Color.Black,
            radius = radius.minus(2.5f),
            center = Offset(
                x = centerX,
                y = centerY
            ),
        )

        rotate(degrees = degrees, pivot = Offset(centerX, centerY)) {
            drawImage(
                image = image,
                dstSize = IntSize(
                    width = radius.toInt() * 2,
                    height = radius.toInt() * 2,
                ),
                dstOffset = IntOffset(
                    x = (centerX - radius).toInt(),
                    y = (centerY - radius).toInt()
                ),
            )
        }
    }
}

@Composable
private fun Basket(
    basket: BasketballEngine.Basket,
    modifier: Modifier = Modifier.zIndex(0f)
) {
    val imageBitmap = ImageBitmap.imageResource(R.drawable.icon_basket)
    val width = (basket.width + basket.radius * 2).toInt()
    val height =  imageBitmap.height * width / imageBitmap.width

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
//        drawLine(
//            color = Color.Blue,
//            start = Offset(
//                x = basket.leftX.value,
//                y = basket.leftY.value
//            ),
//            end = Offset(
//                x = basket.rightX.value,
//                y = basket.rightY.value
//            ),
//            strokeWidth = basket.height
//        )
//
//        drawCircle(
//            color = Color.Blue,
//            radius = basket.radius,
//            center = Offset(
//                x = basket.leftX.value,
//                y = basket.leftY.value
//            ),
//        )
//
//        drawCircle(
//            color = Color.Blue,
//            radius = basket.radius,
//            center = Offset(
//                x = basket.rightX.value,
//                y = basket.rightY.value
//            ),
//        )

        drawImage(
            image = imageBitmap,
            dstSize = IntSize(
                width = width,
                height = height,
            ),
            dstOffset = IntOffset(
                x = (basket.currentX.value - (basket.width + basket.radius * 2) / 2).toInt(),
                y = (basket.currentY.value - basket.height / 2).toInt(),
            ),
        )
    }
}

@Composable
private fun TrainingBasketballLoadingBar(
    modifier: Modifier = Modifier.zIndex(0f),
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        LoadingBar()
    }
}
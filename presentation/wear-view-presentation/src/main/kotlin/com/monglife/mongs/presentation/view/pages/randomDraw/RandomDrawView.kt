package com.monglife.mongs.presentation.view.pages.randomDraw

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.RandomDrawBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.dialog.pages.randomDraw.RandomDrawEnteringDialog
import com.monglife.mongs.presentation.view.dialog.pages.randomDraw.RandomDrawOverDialog
import com.monglife.mongs.presentation.viewmodel.pages.randomDraw.RandomDrawViewModel
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun RandomDrawView(
    navController: NavController,
    randomDrawViewModel: RandomDrawViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = randomDrawViewModel.uiState.collectAsState()
    val currentMongVo = randomDrawViewModel.currentMongVo.collectAsState()
    val randomDrawVo = randomDrawViewModel.randomDrawVo.collectAsState()
    val randomDrawPayPoint = randomDrawViewModel.randomDrawPayPoint.collectAsState()

    Box {
        RandomDrawBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                RandomDrawContent(randomDrawViewModel = randomDrawViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.enteringDialogOpen) {
                    currentMongVo.value?.let {
                        RandomDrawEnteringDialog(
                            randomDrawPayPoint = randomDrawPayPoint.value,
                            payPoint = it.payPoint,
                            randomDrawTicketCount = it.randomDrawTicketCount,
                            onDrawClick = randomDrawViewModel::randomDrawConfirmDialogOpen,
                        )
                    }
                } else if (uiState.value.confirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "뽑기를\n하시겠습니까?",
                        cancel = randomDrawViewModel::randomDrawConfirmDialogClose,
                        confirm = {
                            currentMongVo.value?.let {
                                randomDrawViewModel.randomDraw(mongId = it.mongId)
                            }
                        },
                    )
                } else if (uiState.value.randomDrawDetailDialogOpen) {
                    randomDrawVo.value?.let {
                        RandomDrawOverDialog(
                            randomDrawVo = it,
                            onEndClick = randomDrawViewModel::randomDrawDetailDialogClose,
                        )
                    }
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        randomDrawViewModel.uiEvent.collect { event ->
            when (event) {
                is RandomDrawViewModel.UiEvent.NavMain -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }

                else -> {}
            }
        }
    }
}

private const val DRAW_MACHINE_MAX_DEGREE = 7f

@Composable
private fun RandomDrawContent(
    modifier: Modifier = Modifier,
    randomDrawViewModel: RandomDrawViewModel
) {
    val uiState = randomDrawViewModel.uiState.collectAsState()

    // animation
    val currentRotation = remember { mutableFloatStateOf(0f) }
    val rotation = remember { Animatable(currentRotation.floatValue) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.icon_luck_draw),
            contentDescription = null,
            modifier = Modifier
                .width(90.dp)
                .height(155.dp)
                .rotate(degrees = rotation.value),
            contentScale = ContentScale.FillBounds,
        )
    }

    // for rotate animation
    LaunchedEffect(uiState.value.drawLoading) {
        if (uiState.value.drawLoading) {
            rotation.animateTo(
                targetValue = DRAW_MACHINE_MAX_DEGREE,
                animationSpec = tween(250, easing = FastOutSlowInEasing),
            ) { currentRotation.floatValue = value }

            rotation.animateTo(
                targetValue = currentRotation.floatValue * -1,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            ) { currentRotation.floatValue = value }
        } else if (currentRotation.floatValue != 0f) {
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            ) { currentRotation.floatValue = value }
        }
    }
}
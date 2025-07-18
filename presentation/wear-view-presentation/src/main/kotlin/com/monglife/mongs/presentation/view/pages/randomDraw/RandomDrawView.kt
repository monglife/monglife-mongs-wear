package com.monglife.mongs.presentation.view.pages.randomDraw

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.viewmodel.pages.randomDraw.RandomDrawViewModel

@Composable
internal fun RandomDrawView(
    randomDrawViewModel: RandomDrawViewModel = hiltViewModel(),
) {
    val uiState = randomDrawViewModel.uiState.collectAsState()
    val currentMongVo = randomDrawViewModel.currentMongVo.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                RandomDrawContent(randomDrawViewModel = randomDrawViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.confirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "랜덤 뽑기를\n하시겠습니까?",
                        cancel = randomDrawViewModel::randomDrawConfirmDialogClose,
                        confirm = {
                            currentMongVo.value?.let {
                                randomDrawViewModel.randomDraw(mongId = it.mongId)
                            }
                        },
                    )
                } else if (uiState.value.randomDrawDetailDialogOpen) {
                    // TODO: 랜덤 뽑기 결과 다이얼로그 화면 개발 필요
                }
            }
        }
    }
}

@Composable
private fun RandomDrawContent(
    modifier: Modifier = Modifier,
    randomDrawViewModel: RandomDrawViewModel
) {
    val currentMongVo = randomDrawViewModel.currentMongVo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        // TODO: 화면 개발 필요

        BlueButton(
            text = "뽑기",
            onClick = randomDrawViewModel::randomDrawConfirmDialogOpen,
        )
    }
}
package com.monglife.mongs.presentation.view.pages.searchMap

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
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.viewmodel.pages.searchMap.SearchMapViewModel

@Composable
internal fun SearchMapView(
    searchMapViewModel: SearchMapViewModel = hiltViewModel(),
) {
    val uiState = searchMapViewModel.uiState.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                SearchMapContent(searchMapViewModel = searchMapViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.confirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "맵을 탐색\n하시겠습니까?",
                        cancel = searchMapViewModel::searchMapConfirmDialogClose,
                        confirm = searchMapViewModel::searchMap,
                    )
                } else if (uiState.value.searchDetailDialogOpen) {
                    // TODO: 맵 탐색 결과 다이얼로그 화면 개발 필요
                }
            }
        }
    }
}

@Composable
private fun SearchMapContent(
    modifier: Modifier = Modifier,
    searchMapViewModel: SearchMapViewModel,
) {
    val uiState = searchMapViewModel.uiState.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {

    }
}
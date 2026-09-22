package com.monglife.mongs.presentation.view.pages.help

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.HelpResourceCode
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.chip.Chip
import com.monglife.mongs.presentation.view.dialog.pages.help.HelpDialog
import com.monglife.mongs.presentation.viewmodel.pages.help.HelpViewModel

@Composable
internal fun HelpView(
    navController: NavController,
    helpViewModel: HelpViewModel = hiltViewModel(),
) {
    val uiState = helpViewModel.uiState.collectAsStateWithLifecycle()
    val currentHelpVo = helpViewModel.currentHelpVo.collectAsStateWithLifecycle()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                HelpContent(
                    helpViewModel = helpViewModel,
                    onGuideClick = { navController.navigate(RouterPath.Guide.route) },
                )
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.detailDialogOpen) {
                    currentHelpVo.value?.let { helpVo ->
                        HelpDialog(
                            icon = helpVo.helpIconCode?.let { HelpResourceCode.getResourceCode(it) },
                            contents = helpVo.contents,
                            cancel = helpViewModel::helpDialogClose
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpContent(
    modifier: Modifier = Modifier,
    helpViewModel: HelpViewModel,
    onGuideClick: () -> Unit,
) {
    val helpVos = helpViewModel.helpVos.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 1)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        PositionIndicator(scalingLazyListState = listState)
        ScalingLazyColumn(
            contentPadding = PaddingValues(vertical = 60.dp, horizontal = 6.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState,
            autoCentering = null,
        ) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Text(
                        text = "도움말",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            }

            /**
             * 가이드는 다이얼로그가 아니라 별도 화면이라 목록의 다른 항목과 동작이 다르다.
             * 처음 한 번은 자동으로 뜨지만, 그때 넘겨 버린 사람을 위해 입구를 남겨 둔다.
             */
            item {
                Chip(
                    fontColor = Color.White,
                    backgroundColor = Color.Black,
                    label = "가이드 다시 보기",
                    secondaryLabel = "처음 안내를 다시 봐요",
                    onClick = onGuideClick,
                )
            }

            for (helpVo in helpVos.value) {
                item {
                    Chip(
                        fontColor = Color.White,
                        backgroundColor = Color.Black,
                        label = helpVo.title,
                        secondaryLabel = helpVo.subTitle,
                        onClick = { helpViewModel.helpDialogOpen(helpVo) },
                    )
                }
            }
        }
    }
}
package com.monglife.mongs.presentation.view.pages.help

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.HelpResourceCode
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.chip.Chip
import com.monglife.mongs.presentation.view.dialog.pages.help.HelpDialog
import com.monglife.mongs.presentation.viewmodel.pages.help.HelpViewModel

@Composable
internal fun HelpView(
    helpViewModel: HelpViewModel = hiltViewModel(),
) {
    val uiState = helpViewModel.uiState.collectAsState()
    val currentHelpVo = helpViewModel.currentHelpVo.collectAsState()
    val helpVos = helpViewModel.helpVos.collectAsState()

    val listState = rememberScalingLazyListState(initialCenterItemIndex = 1)

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
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

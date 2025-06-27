package com.monglife.mongs.presentation.view.pages.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.chip.Chip
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.dialog.common.ConfirmDialog
import com.monglife.mongs.presentation.view.dialog.pages.feedback.CreateFeedbackDialog
import com.monglife.mongs.presentation.viewmodel.pages.feedback.FeedbackViewModel

@Composable
fun FeedbackView(
    feedbackViewModel: FeedbackViewModel = hiltViewModel(),
) {
    val uiState = feedbackViewModel.uiState.collectAsState()
    val feedbackTypeVos = feedbackViewModel.feedbackTypeVos.collectAsState()
    val title = feedbackViewModel.title.collectAsState()
    val content = feedbackViewModel.content.collectAsState()

    val listState = rememberScalingLazyListState(initialCenterItemIndex = 1)

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(
                modifier = Modifier.zIndex(1f),
                contentAlignment = Alignment.Center,
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
                                text = "오류신고",
                                textAlign = TextAlign.Center,
                                fontFamily = DAL_MU_RI,
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp,
                                color = MongsWhite,
                                maxLines = 1,
                            )
                        }
                    }

                    for (feedbackTypeVo in feedbackTypeVos.value) {
                        item {
                            Chip(
                                fontColor = Color.White,
                                backgroundColor = Color.Black,
                                label = feedbackTypeVo.feedbackName,
                                secondaryLabel = feedbackTypeVo.description,
                                onClick = {
                                    feedbackViewModel.createFeedbackDialogOpen(
                                        title = feedbackTypeVo.feedbackName
                                    )
                                },
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.createDialogOpen) {
                    CreateFeedbackDialog(
                        title = title.value,
                        content = content.value,
                        cancel = feedbackViewModel::createFeedbackDialogClose,
                        confirm = feedbackViewModel::createFeedbackConfirmDialogOpen,
                    ) { feedbackViewModel.updateContent(content = it) }
                }
            }

            Box(modifier = Modifier.zIndex(3f)) {
                if (uiState.value.createConfirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "오류를\n전송 하시겠습니까?",
                        cancel = feedbackViewModel::createFeedbackConfirmDialogClose,
                        confirm = {
                            feedbackViewModel.createFeedback(
                                title = title.value,
                                content = content.value,
                            )
                        },
                    )
                } else if (uiState.value.createSuccessDialogOpen) {
                    ConfirmDialog(
                        text = "오류 전송 완료!\n처리결과는 메일로\n전달드리겠습니다.",
                        confirm = feedbackViewModel::createFeedbackSuccessDialogClose,
                    )
                }
            }
        }
    }
}
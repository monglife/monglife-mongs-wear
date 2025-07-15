package com.monglife.mongs.presentation.view.pages.notice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
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
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.chip.Chip
import com.monglife.mongs.presentation.view.dialog.pages.notice.NoticeDetailDialog
import com.monglife.mongs.presentation.view.utils.TimeUtil
import com.monglife.mongs.presentation.viewmodel.pages.notice.NoticeViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
internal fun NoticeView(
    noticeViewModel: NoticeViewModel = hiltViewModel(),
) {
    val uiState = noticeViewModel.uiState.collectAsState()
    val page = noticeViewModel.page.collectAsState()
    val isLastPage = noticeViewModel.isLastPage.collectAsState()
    val noticeVos = noticeViewModel.noticeVos.collectAsState()
    val content = noticeViewModel.content.collectAsState()

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
                    .zIndex(1f),
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
                                text = "공지사항",
                                textAlign = TextAlign.Center,
                                fontFamily = DAL_MU_RI,
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp,
                                color = MongsWhite,
                                maxLines = 1,
                            )
                        }
                    }

                    for (noticeVo in noticeVos.value) {
                        item {
                            Chip(
                                fontColor = Color.White,
                                backgroundColor = Color.Black,
                                label = noticeVo.title,
                                secondaryLabel = "[${noticeVo.writerName}] ${
                                    TimeUtil.localDateTimeToString(
                                        noticeVo.createdAt
                                    )
                                }",
                                onClick = { noticeViewModel.noticeDetailDialogOpen(content = noticeVo.content) },
                            )
                        }
                    }

                    item {
                        if (uiState.value.listLoadingBar) {
                            LoadingBar(width = 25, height = 25)
                        }
                    }
                }
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.detailDialogOpen) {
                    NoticeDetailDialog(
                        content = content.value,
                        confirm = noticeViewModel::noticeDetailDialogClose
                    )
                }
            }
        }
    }

    // 스크롤 위치 감지
    LaunchedEffect(listState) {
        snapshotFlow {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            if (visibleItems.isEmpty()) return@snapshotFlow false
            val lastVisibleIndex = visibleItems.last().index
            lastVisibleIndex >= totalItemsCount - 1
        }.distinctUntilChanged().filter { it }.collect {
            // 마지막 페이지 아닌 경우만 재조회
            if (!isLastPage.value) {
                noticeViewModel.changePage(page = page.value + 1)
            }
        }
    }
}
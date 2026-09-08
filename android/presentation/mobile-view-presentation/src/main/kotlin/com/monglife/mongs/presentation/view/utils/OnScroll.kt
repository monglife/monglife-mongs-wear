package com.monglife.mongs.presentation.view.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
internal fun OnScroll(
    listState: LazyListState,
    onScrollEnd: () -> Unit,
) {
    // 스크롤 위치 감지
    LaunchedEffect(listState) {
        snapshotFlow {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            if (visibleItems.isEmpty()) return@snapshotFlow false
            val lastVisibleIndex = visibleItems.last().index
            lastVisibleIndex >= totalItemsCount - 1
        }.distinctUntilChanged().filter { it }.collect {
            onScrollEnd()
        }
    }
}
package com.monglife.mongs.presentation.view.component.common.textbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.pages.main.component.pixelBox

private val ITEM_HEIGHT = 36.dp

/**
 * 시/분 선택 휠.
 *
 * wear 는 ScalingLazyColumn + AutoCenteringParams 로 만들었는데 Material2 에 대응물이 없다.
 * 스냅 fling 을 붙인 LazyColumn 이 같은 상호작용을 준다.
 *
 * wear 에는 스크롤이 멈출 때마다 animateScrollToItem 으로 되돌리는 LaunchedEffect 가 있었다.
 * 사용자의 플링과 싸우는 코드라 옮기지 않는다 - rememberSnapFlingBehavior 가 그 일을 제대로 한다.
 *
 * 값 범위는 wear 와 같다. 호출부가 "%02d:%02d" 로 만들어 LocalTime.parse 에 넘긴다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TimeWheel(
    modifier: Modifier = Modifier,
    initValue: Int = 0,
    valueRange: List<Int>,
    changeValue: (Int) -> Unit = {},
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = valueRange.indexOf(initValue).coerceAtLeast(0)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.height(ITEM_HEIGHT * 3),
    ) {
        // 가운데 선택 밴드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ITEM_HEIGHT)
                .zIndex(0f)
                .pixelBox(
                    fill = MongsWhite.copy(alpha = 0.12f),
                    border = MongsWhite.copy(alpha = 0.5f),
                )
        )

        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            contentPadding = PaddingValues(vertical = ITEM_HEIGHT),
            modifier = Modifier.fillMaxWidth().zIndex(1f),
        ) {
            itemsIndexed(valueRange) { index, value ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(ITEM_HEIGHT),
                ) {
                    Text(
                        text = value.toString().padStart(2, '0'),
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 26.sp,
                        color = if (index == listState.firstVisibleItemIndex) MongsWhite
                                else MongsWhite.copy(alpha = 0.35f),
                    )
                }
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        valueRange.getOrNull(listState.firstVisibleItemIndex)?.let(changeValue)
    }
}

package com.monglife.mongs.presentation.view.pages.collection

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.PositionIndicator
import com.monglife.mongs.presentation.view.assets.MapResourceCode
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.CircleTextButton
import com.monglife.mongs.presentation.view.dialog.pages.collection.CollectionMongDetailDialog
import com.monglife.mongs.presentation.viewmodel.pages.collection.CollectionMapViewModel
import com.monglife.mongs.presentation.wear.component.common.button.CircleImageButton
import com.mongs.wear.presentation.dialog.collection.CollectionMapDetailDialog
import com.mongs.wear.presentation.view.wear.R
import kotlin.math.min

@Composable
fun CollectionMapView(
    collectionMapViewModel: CollectionMapViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = collectionMapViewModel.uiState.collectAsState()
    val collectionMapVos = collectionMapViewModel.collectionMapVos.collectAsState()
    val detailCollectionMapVo = collectionMapViewModel.detailCollectionMapVo.collectAsState()

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().zIndex(1f),
            ) {
                PositionIndicator(lazyListState = listState)
                LazyColumn(
                    verticalArrangement = Arrangement.Center,
                    contentPadding = PaddingValues(vertical = 40.dp),
                    state = listState,
                ) {
                    for (startIndex: Int in 1..collectionMapVos.value.size step (3)) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier
                                    .height(59.dp)
                                    .padding(bottom = 5.dp)
                            ) {
                                for (index: Int in startIndex..min(
                                    collectionMapVos.value.size,
                                    startIndex + 2
                                )) {
                                    val collectionMapVo = collectionMapVos.value[index - 1]

                                    if (!collectionMapVo.isIncluded) {
                                        CircleTextButton(
                                            text = "?",
                                            border = R.drawable.btn_border_purple_dark,
                                            onClick = {
                                                Toast.makeText(
                                                    context,
                                                    "수집하지 않은 맵",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            },
                                            modifier = Modifier
                                                .offset(
                                                    y = if (index % 3 == 2) (-27).dp else 0.dp,
                                                    x = 0.dp
                                                )
                                        )
                                    } else {
                                        CircleImageButton(
                                            icon = MapResourceCode.getResource(collectionMapVo.code).code,
                                            border = R.drawable.btn_border_purple_dark,
                                            onClick = { collectionMapViewModel.collectionMapDetailDialogOpen(collectionMapVo = collectionMapVo) },
                                            modifier = Modifier
                                                .offset(
                                                    y = if (index % 3 == 2) (-27).dp else 0.dp,
                                                    x = 0.dp
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.value.collectionMapDetailDialogOpen) {
                detailCollectionMapVo.value?.let {
                    CollectionMapDetailDialog(
                        modifier = Modifier.zIndex(2f),
                        collectionMapVo = it,
                        onSetClick = { mapCode -> collectionMapViewModel.setBackgroundMapCode(mapCode = mapCode) },
                        onClick = collectionMapViewModel::collectionMapDetailDialogClose,
                    )
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        collectionMapViewModel.uiEvent.collect { event ->
            when (event) {
                is CollectionMapViewModel.UiEvent.SetBackground -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}

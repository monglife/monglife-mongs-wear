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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.PositionIndicator
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.CircleTextButton
import com.monglife.mongs.presentation.view.dialog.pages.collection.CollectionMongDetailDialog
import com.monglife.mongs.presentation.viewmodel.pages.collection.CollectionMongViewModel
import com.monglife.mongs.presentation.wear.component.common.button.CircleImageButton
import com.mongs.wear.presentation.view.wear.R
import kotlin.math.min

@Composable
fun CollectionMongView(
    collectionMongViewModel: CollectionMongViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = collectionMongViewModel.uiState.collectAsState()
    val collectionMongVos = collectionMongViewModel.collectionMongVos.collectAsState()
    val detailCollectionMongVo = collectionMongViewModel.detailCollectionMongVo.collectAsState()

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
                    for (startIndex: Int in 1..collectionMongVos.value.size step (3)) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier
                                    .height(59.dp)
                                    .padding(bottom = 5.dp)
                            ) {
                                for (index: Int in startIndex..min(
                                    collectionMongVos.value.size,
                                    startIndex + 2
                                )) {
                                    val collectionMongVo = collectionMongVos.value[index - 1]

                                    if (!collectionMongVo.isIncluded) {
                                        CircleTextButton(
                                            text = "?",
                                            border = R.drawable.btn_border_purple_dark,
                                            onClick = {
                                                Toast.makeText(
                                                    context,
                                                    "수집하지 않은 몽",
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
                                            icon = MongResourceCode.getResource(collectionMongVo.code).pngCode,
                                            border = R.drawable.btn_border_purple_dark,
                                            onClick = { collectionMongViewModel.collectionMongDetailDialogOpen(collectionMongVo = collectionMongVo) },
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

            if (uiState.value.collectionMongDetailDialogOpen) {
                detailCollectionMongVo.value?.let {
                    CollectionMongDetailDialog(
                        modifier = Modifier.zIndex(2f),
                        collectionMongVo = it,
                        onClick = collectionMongViewModel::collectionMongDetailDialogClose,
                    )
                }
            }
        }
    }
}

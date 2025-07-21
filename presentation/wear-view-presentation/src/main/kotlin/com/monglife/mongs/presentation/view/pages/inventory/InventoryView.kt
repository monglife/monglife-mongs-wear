package com.monglife.mongs.presentation.view.pages.inventory

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsLightGray
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.SelectButton
import com.monglife.mongs.presentation.view.component.common.pagenation.PageIndicator
import com.monglife.mongs.presentation.view.component.pages.inventory.InventoryItem
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.viewmodel.pages.inventory.InventoryViewModel

@Composable
internal fun InventoryView(
    navController: NavController,
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = inventoryViewModel.uiState.collectAsState()
    val currentMongVo = inventoryViewModel.currentMongVo.collectAsState()
    val currentInventoryVo = inventoryViewModel.currentInventoryVo.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                InventoryContent(inventoryViewModel = inventoryViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.confirmDialogOpen) {
                    currentInventoryVo.value?.let { inventoryVo ->
                        ConfirmAndCancelDialog(
                            text = "${inventoryVo.inventoryName}\n사용하시겠습니까?",
                            cancel = inventoryViewModel::consumeInventoryConfirmDialogClose,
                            confirm = {
                                currentMongVo.value?.let { mongVo ->
                                    inventoryViewModel.consumeInventory(
                                        mongId = mongVo.mongId,
                                        inventoryId = inventoryVo.inventoryId,
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        inventoryViewModel.uiEvent.collect { event ->
            when (event) {
                is InventoryViewModel.UiEvent.NavMenu -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }
                else -> {}
            }
        }
    }
}

private const val INVENTORY_ROUND_SIZE = 10
private const val INVENTORY_WIDTH = 110
private const val INVENTORY_HEIGHT = 130
private const val INVENTORY_BAR_WIDTH = 62
private const val INVENTORY_BAR_HEIGHT = 30

@Composable
private fun InventoryContent(
    modifier: Modifier = Modifier,
    inventoryViewModel: InventoryViewModel,
) {
    val page = inventoryViewModel.page.collectAsState()
    val totalPage = inventoryViewModel.totalPage.collectAsState()
    val inventoryVos = inventoryViewModel.inventoryVos.collectAsState()
    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = page.value - 1
            override val pageCount: Int
                get() = totalPage.value
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.zIndex(1f)) {
            PageIndicator(
                pageIndicatorState = pageIndicatorState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 5.dp)
                    .zIndex(1f)
            )
        }

        Box(modifier = Modifier.zIndex(2f)) {
            if (totalPage.value > 0) {
                SelectButton(
                    leftBtnDisabled = page.value <= 1,
                    rightBtnDisabled = page.value >= totalPage.value,
                    leftBtnClick = inventoryViewModel::prevPage,
                    rightBtnClick = inventoryViewModel::nextPage,
                )
            }
        }

        Box(modifier = Modifier.zIndex(3f)) {
            Box(
                modifier = Modifier
                    .size(width = INVENTORY_WIDTH.dp, height = INVENTORY_HEIGHT.dp)
                    .zIndex(1f)
                    .clip(RoundedCornerShape(INVENTORY_ROUND_SIZE.dp))
                    .background(color = Color.LightGray)
                    .align(Alignment.BottomCenter)
            )

            Box(
                modifier = Modifier
                    .size(width = INVENTORY_WIDTH.dp, height = INVENTORY_BAR_HEIGHT.dp)
                    .zIndex(2f)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = INVENTORY_ROUND_SIZE.dp,
                                    topEnd = INVENTORY_ROUND_SIZE.dp
                                )
                            )
                            .size(width = INVENTORY_BAR_WIDTH.dp, height = INVENTORY_BAR_HEIGHT.dp)
                            .background(color = MongsLightGray)
                    ) {
                        Text(
                            text = "인벤토리",
                            textAlign = TextAlign.Center,
                            fontFamily = DAL_MU_RI,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = Color.Black,
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            bottomStart = INVENTORY_ROUND_SIZE.dp,
                            bottomEnd = INVENTORY_ROUND_SIZE.dp
                        )
                    )
                    .background(color = MongsLightGray)
                    .size(
                        width = INVENTORY_WIDTH.dp,
                        height = (INVENTORY_HEIGHT - INVENTORY_BAR_HEIGHT).dp
                    )
                    .zIndex(3f)
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = modifier.padding(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(INVENTORY_WIDTH.dp)
                            .weight(0.5f)
                    ) {
                        InventoryItem(inventoryVo = if (inventoryVos.value.isNotEmpty()) inventoryVos.value[0] else null) {
                            inventoryViewModel.consumeInventoryConfirmDialogOpen(inventoryVo = it)
                        }
                        InventoryItem(inventoryVo = if (inventoryVos.value.size > 1) inventoryVos.value[1] else null) {
                            inventoryViewModel.consumeInventoryConfirmDialogOpen(inventoryVo = it)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(INVENTORY_WIDTH.dp)
                            .weight(0.5f)
                    ) {
                        InventoryItem(inventoryVo = if (inventoryVos.value.size > 2) inventoryVos.value[2] else null) {
                            inventoryViewModel.consumeInventoryConfirmDialogOpen(inventoryVo = it)
                        }
                        InventoryItem(inventoryVo = if (inventoryVos.value.size > 3) inventoryVos.value[3] else null) {
                            inventoryViewModel.consumeInventoryConfirmDialogOpen(inventoryVo = it)
                        }
                    }
                }
            }
        }
    }
}
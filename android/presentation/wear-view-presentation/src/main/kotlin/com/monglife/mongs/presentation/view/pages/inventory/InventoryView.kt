package com.monglife.mongs.presentation.view.pages.inventory

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Text
import com.monglife.mongs.application.mong.vo.InventoryVo
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.FoodResourceCode
import com.monglife.mongs.presentation.view.assets.MongsDarkGray
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.assets.SnackResourceCode
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.button.SelectButton
import com.monglife.mongs.presentation.view.component.common.pagenation.PageIndicator
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.dialog.pages.feed.FeedItemDetailDialog
import com.monglife.mongs.presentation.viewmodel.pages.inventory.InventoryViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainSlotViewModel

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun InventoryView(
    navController: NavController,
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainSlotViewModel: MainSlotViewModel = hiltViewModel<MainSlotViewModel>(parentEntry)

    val uiState = inventoryViewModel.uiState.collectAsStateWithLifecycle()
    val currentMongVo = inventoryViewModel.currentMongVo.collectAsStateWithLifecycle()
    val currentInventoryVo = inventoryViewModel.currentInventoryVo.collectAsStateWithLifecycle()
    val currentInventoryStatusVo = inventoryViewModel.currentInventoryStatusVo.collectAsStateWithLifecycle()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                InventoryContent(inventoryViewModel = inventoryViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                currentInventoryVo.value?.let { inventoryVo ->
                    if (uiState.value.detailDialogOpen) {
                        currentInventoryStatusVo.value?.let { statusVo ->
                            FeedItemDetailDialog(
                                weight = statusVo.weight,
                                strength = statusVo.strength,
                                satiety = statusVo.satiety,
                                healthy = statusVo.healthy,
                                fatigue = statusVo.fatigue,
                                onClick = inventoryViewModel::detailDialogClose,
                            )
                        }
                    } else if (uiState.value.confirmDialogOpen) {
                        ConfirmAndCancelDialog(
                            text = "${inventoryVo.inventoryName}\n사용하시겠습니까?",
                            cancel = inventoryViewModel::consumeConfirmDialogClose,
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
                is InventoryViewModel.UiEvent.NavMain -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }

                is InventoryViewModel.UiEvent.Consume -> {
                    mainSlotViewModel.eatingEvent()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun InventoryContent(
    modifier: Modifier = Modifier,
    inventoryViewModel: InventoryViewModel,
) {
    val currentInventoryVo = inventoryViewModel.currentInventoryVo.collectAsStateWithLifecycle()
    val currentInventoryStatusVo = inventoryViewModel.currentInventoryStatusVo.collectAsStateWithLifecycle()
    val inventoryVos = inventoryViewModel.inventoryVos.collectAsStateWithLifecycle()
    val inventoryVoIndex = inventoryViewModel.inventoryVoIndex.collectAsStateWithLifecycle()
    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = inventoryVoIndex.value
            override val pageCount: Int
                get() = inventoryVos.value.size
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        currentInventoryVo.value?.let { inventoryVo ->
            InventorySection(
                modifier = Modifier.zIndex(1f),
                inventoryVo = inventoryVo,
                // 밥/간식 목록에 없는 코드면 보여 줄 스텟이 없어 아이콘 클릭을 막는다.
                hasStatus = currentInventoryStatusVo.value != null,
                detailDialogOpen = inventoryViewModel::detailDialogOpen,
                consumeDialogOpen = inventoryViewModel::consumeConfirmDialogOpen,
            )
        } ?: EmptyInventorySection(modifier = Modifier.zIndex(1f))

        // 아이템이 없으면 넘길 곳도 없다
        if (inventoryVos.value.isNotEmpty()) {
            PageIndicator(
                pageIndicatorState = pageIndicatorState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 5.dp)
                    .zIndex(1f)
            )

            SelectButton(
                modifier = Modifier.zIndex(2f),
                leftBtnDisabled = inventoryVoIndex.value == 0,
                rightBtnDisabled = inventoryVoIndex.value == inventoryVos.value.size - 1,
                leftBtnClick = inventoryViewModel::prevInventory,
                rightBtnClick = inventoryViewModel::nextInventory,
            )
        }
    }
}

@Composable
private fun InventorySection(
    modifier: Modifier = Modifier,
    inventoryVo: InventoryVo,
    hasStatus: Boolean,
    detailDialogOpen: () -> Unit,
    consumeDialogOpen: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            InventoryTitle(modifier = Modifier.weight(0.2f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
            ) {
                Text(
                    text = inventoryVo.inventoryName,
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = MongsDarkGray,
                    maxLines = 1,
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
            ) {
                val iconModifier = Modifier
                    .size(50.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = hasStatus,
                        onClick = detailDialogOpen,
                    )

                inventoryResourceCodeOf(inventoryVo = inventoryVo)?.let { resourceCode ->
                    Image(
                        painter = painterResource(resourceCode),
                        contentDescription = null,
                        modifier = iconModifier,
                    )
                } ?: Text(
                    text = "?",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp,
                    color = MongsWhite,
                    modifier = iconModifier,
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
            ) {
                BlueButton(
                    text = "사용",
                    width = 70,
                    onClick = consumeDialogOpen,
                )
            }

            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun EmptyInventorySection(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            InventoryTitle(modifier = Modifier.weight(0.2f))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
            ) {
                Text(
                    text = "비어 있어요",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = MongsDarkGray,
                    maxLines = 1,
                )
            }

            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

/**
 * 어느 화면인지 알려 주는 제목. 아이템이 있든 없든 같은 자리에 온다.
 */
@Composable
private fun InventoryTitle(
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "인벤토리",
            textAlign = TextAlign.Center,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 14.sp,
            color = MongsWhite,
            maxLines = 1,
        )
    }
}

/**
 * 맵 아이템은 그릴 글리프가 없어 '?' 로 떨어진다.
 */
private fun inventoryResourceCodeOf(inventoryVo: InventoryVo): Int? =
    when (inventoryVo.inventoryTypeCode) {
        InventoryTypeCode.FOOD -> FoodResourceCode.getResourceCode(inventoryVo.inventoryCode)
        InventoryTypeCode.SNACK -> SnackResourceCode.getResourceCode(inventoryVo.inventoryCode)
        InventoryTypeCode.MAP -> null
    }

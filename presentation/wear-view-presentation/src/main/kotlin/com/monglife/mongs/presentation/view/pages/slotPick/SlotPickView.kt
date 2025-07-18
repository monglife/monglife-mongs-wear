package com.monglife.mongs.presentation.view.pages.slotPick

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.PageIndicatorState
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.SelectButton
import com.monglife.mongs.presentation.view.component.common.pagenation.PageIndicator
import com.monglife.mongs.presentation.view.component.pages.slotPick.BuySlot
import com.monglife.mongs.presentation.view.component.pages.slotPick.EmptySlot
import com.monglife.mongs.presentation.view.component.pages.slotPick.Slot
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.dialog.pages.slotPick.CreateSlotDialog
import com.monglife.mongs.presentation.view.dialog.pages.slotPick.SlotDetailDialog
import com.monglife.mongs.presentation.viewmodel.pages.main.MainPagerViewModel
import com.monglife.mongs.presentation.viewmodel.pages.slotPick.SlotPickViewModel
import com.monglife.mongs.presentation.viewmodel.pages.slotPick.vo.SlotVo

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun SlotPickView(
    navController: NavController,
    slotPickViewModel: SlotPickViewModel = hiltViewModel(),
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainPagerViewModel: MainPagerViewModel = hiltViewModel<MainPagerViewModel>(parentEntry)

    val uiState = slotPickViewModel.uiState.collectAsState()
    val currentSlotVo = slotPickViewModel.currentSlotVo.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                SlotPickContent(slotPickViewModel = slotPickViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.createDialogOpen) {
                    CreateSlotDialog(
                        onCreateClick = { name, sleepAt, wakeupAt ->
                            slotPickViewModel.createMong(
                                name,
                                sleepAt,
                                wakeupAt
                            )
                        },
                        onCloseClick = slotPickViewModel::initialize,
                    )
                } else if (uiState.value.buySlotConfirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "새로운 슬롯을\n구매하시겠습니까?",
                        confirm = { slotPickViewModel.buySlot() },
                        cancel = slotPickViewModel::initialize,
                    )
                } else {
                    currentSlotVo.value?.mongVo?.let {
                        if (uiState.value.detailDialogOpen) {
                            SlotDetailDialog(
                                mongId = it.mongId,
                                statusCode = it.statusCode,
                                stateCode = it.stateCode,
                                isSleep = it.isSleep,
                                weight = it.weight,
                                healthyRatio = it.healthyRatio,
                                satietyRatio = it.satietyRatio,
                                strengthRatio = it.strengthRatio,
                                fatigueRatio = it.fatigueRatio,
                                payPoint = it.payPoint,
                                born = it.createdAt,
                                onClick = slotPickViewModel::initialize,
                            )
                        } else if (uiState.value.deleteConfirmDialogOpen) {
                            ConfirmAndCancelDialog(
                                text = "현재 몽을\n삭제하시겠습니까?",
                                confirm = { slotPickViewModel.deleteMong(mongId = it.mongId) },
                                cancel = slotPickViewModel::initialize
                            )
                        } else if (uiState.value.pickConfirmDialogOpen) {
                            ConfirmAndCancelDialog(
                                text = "현재 몽을\n선택하시겠습니까?",
                                confirm = {
                                    slotPickViewModel.pickMong(mongId = it.mongId)
                                    mainPagerViewModel.pagerScroll(MainPagerViewModel.NORMAL_PAGER_STATE_INIT)
                                    navController.navigate(RouterPath.Main.route) {
                                        popUpTo(
                                            RouterPath.Main.route
                                        ) { inclusive = true }
                                    }
                                },
                                cancel = slotPickViewModel::initialize
                            )
                        } else if (uiState.value.graduateConfirmDialogOpen) {
                            ConfirmAndCancelDialog(
                                text = "현재 몽을\n졸업시키시겠습니까?",
                                confirm = { slotPickViewModel.graduateMong(mongId = it.mongId) },
                                cancel = slotPickViewModel::initialize
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotPickContent(
    modifier: Modifier = Modifier,
    slotPickViewModel: SlotPickViewModel,
) {
    val starPoint = slotPickViewModel.starPoint.collectAsState()
    val currentMongVo = slotPickViewModel.currentMongVo.collectAsState()
    val slotIndex = slotPickViewModel.slotVoIndex.collectAsState()
    val slotVos = slotPickViewModel.slotVos.collectAsState()
    val currentSlotVo = slotPickViewModel.currentSlotVo.collectAsState()
    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = slotIndex.value
            override val pageCount: Int
                get() = slotVos.value.size
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        PageIndicator(
            pageIndicatorState = pageIndicatorState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 5.dp)
                .zIndex(1f)
        )

        currentSlotVo.value?.let { slotVo ->
            when (slotVo.type) {
                SlotVo.SlotType.EXISTS -> {
                    slotVo.mongVo?.let {
                        Slot(
                            modifier = Modifier.zIndex(1f),
                            currentMongId = currentMongVo.value?.mongId,
                            mongVo = it,
                            detailDialogOpen = slotPickViewModel::detailDialogOpen,
                            graduateDialogOpen = slotPickViewModel::graduateConfirmDialogOpen,
                            deleteDialogOpen = slotPickViewModel::deleteConfirmDialogOpen,
                            pickDialogOpen = slotPickViewModel::pickConfirmDialogOpen,
                        )
                    }
                }

                SlotVo.SlotType.EMPTY -> EmptySlot(
                    modifier = Modifier.zIndex(1f),
                    createDialogOpen = slotPickViewModel::createDialogOpen
                )

                SlotVo.SlotType.BUY -> BuySlot(
                    modifier = Modifier.zIndex(1f),
                    starPoint = starPoint.value,
                    buySlotPrice = 10,
                    buySlotDialogOpen = slotPickViewModel::buySlotConfirmDialogOpen,
                )
            }
        }

        SelectButton(
            modifier = Modifier.zIndex(3f),
            leftBtnDisabled = slotIndex.value == 0,
            rightBtnDisabled = slotIndex.value == slotVos.value.size - 1,
            leftBtnClick = slotPickViewModel::prevSlot,
            rightBtnClick = slotPickViewModel::nextSlot,
        )
    }
}
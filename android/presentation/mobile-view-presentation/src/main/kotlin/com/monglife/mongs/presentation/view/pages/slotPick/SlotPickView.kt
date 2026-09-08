package com.monglife.mongs.presentation.view.pages.slotPick

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.assets.SlotPickDimens
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.SelectButton
import com.monglife.mongs.presentation.view.component.common.pagenation.PageIndicator
import com.monglife.mongs.presentation.view.component.common.textbox.StarPointBox
import com.monglife.mongs.presentation.view.component.pages.slotPick.BuySlot
import com.monglife.mongs.presentation.view.component.pages.slotPick.EmptySlot
import com.monglife.mongs.presentation.view.component.pages.slotPick.Slot
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.dialog.pages.slotPick.CreateSlotDialog
import com.monglife.mongs.presentation.view.dialog.pages.slotPick.SlotDetailDialog
import com.monglife.mongs.presentation.view.pages.main.component.mainPanel
import com.monglife.mongs.presentation.viewmodel.pages.slotPick.SlotPickViewModel
import com.monglife.mongs.presentation.viewmodel.pages.slotPick.vo.SlotVo
import com.mongs.presentation.view.mobile.R

private const val BUY_SLOT_PRICE = 10

/**
 * 슬롯 선택.
 *
 * wear 와 같은 캐러셀이다 - 좌우 버튼으로 슬롯 하나씩 넘긴다.
 *
 * wear 원본에서 빠진 것:
 * - mainPagerViewModel.pagerScroll(...) : 모바일엔 페이저가 없다.
 *   그 아래 navigate(Main) { popUpTo(Main) { inclusive = true } } 는 유지한다 -
 *   MainViewModel 이 ObserveCurrentMongUseCase 를 보고 있어 재생성만으로 충분하다.
 * - getBackStackEntry(Root) : 그래프 스코프가 필요한 것이 이 화면엔 없다.
 *
 * 더한 것:
 * - 상단바의 뒤로가기. wear 는 스와이프 dismiss 에 의존했다.
 * - BackHandler. 안 넣으면 열린 다이얼로그에서 뒤로가기가 화면을 통째로 pop 한다.
 */
@Composable
internal fun SlotPickView(
    navController: NavController,
    slotPickViewModel: SlotPickViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState by slotPickViewModel.uiState.collectAsStateWithLifecycle()
    val currentSlotVo by slotPickViewModel.currentSlotVo.collectAsStateWithLifecycle()

    /**
     * 생성 폼 상태를 여기서 잡는다.
     * ViewModel 이 생성 실패 시 UiState.Create 를 다시 세팅하면서 다이얼로그가
     * 컴포지션을 나갔다 들어온다 - 안에서 remember 하면 입력이 날아간다.
     */
    val newName = rememberSaveable { mutableStateOf("") }
    val sleepHour = rememberSaveable { mutableIntStateOf(22) }
    val sleepMinute = rememberSaveable { mutableIntStateOf(0) }
    val wakeHour = rememberSaveable { mutableIntStateOf(7) }
    val wakeMinute = rememberSaveable { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        DefaultBackground()

        if (uiState.loadingBar) {
            LoadingBar()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .zIndex(1f),
                verticalArrangement = Arrangement.spacedBy(SlotPickDimens.BandGap),
            ) {
                SlotPickTopBar(
                    starPoint = slotPickViewModel.starPoint.collectAsStateWithLifecycle().value,
                    onBack = { navController.popBackStack() },
                )

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    SlotPickCarousel(slotPickViewModel = slotPickViewModel)
                }

                PageIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SlotPickDimens.IndicatorHeight),
                    selectedPage = slotPickViewModel.slotVoIndex.collectAsStateWithLifecycle().value,
                    pageCount = slotPickViewModel.slotVos.collectAsStateWithLifecycle().value.size,
                )
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.createDialogOpen) {
                    CreateSlotDialog(
                        name = newName,
                        sleepHour = sleepHour,
                        sleepMinute = sleepMinute,
                        wakeHour = wakeHour,
                        wakeMinute = wakeMinute,
                        onCreateClick = { name, sleepAt, wakeupAt ->
                            slotPickViewModel.createMong(name, sleepAt, wakeupAt)
                        },
                        onCloseClick = slotPickViewModel::initialize,
                    )
                } else if (uiState.buySlotConfirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "새로운 슬롯을\n구매하시겠습니까?",
                        confirm = { slotPickViewModel.buySlot() },
                        cancel = slotPickViewModel::initialize,
                    )
                } else {
                    currentSlotVo?.mongVo?.let {
                        if (uiState.detailDialogOpen) {
                            SlotDetailDialog(mongVo = it, onClick = slotPickViewModel::initialize)
                        } else if (uiState.deleteConfirmDialogOpen) {
                            ConfirmAndCancelDialog(
                                text = "현재 몽을\n삭제하시겠습니까?",
                                confirm = { slotPickViewModel.deleteMong(mongId = it.mongId) },
                                cancel = slotPickViewModel::initialize,
                            )
                        } else if (uiState.pickConfirmDialogOpen) {
                            ConfirmAndCancelDialog(
                                text = "현재 몽을\n선택하시겠습니까?",
                                confirm = {
                                    slotPickViewModel.pickMong(mongId = it.mongId)
                                    navController.navigate(RouterPath.Main.route) {
                                        popUpTo(RouterPath.Main.route) { inclusive = true }
                                    }
                                },
                                cancel = slotPickViewModel::initialize,
                            )
                        } else if (uiState.graduateConfirmDialogOpen) {
                            ConfirmAndCancelDialog(
                                text = "현재 몽을\n졸업시키시겠습니까?",
                                confirm = { slotPickViewModel.graduateMong(mongId = it.mongId) },
                                cancel = slotPickViewModel::initialize,
                            )
                        }
                    }
                }
            }
        }
    }

    // 다이얼로그가 열려 있으면 뒤로가기는 그것만 닫는다.
    BackHandler(enabled = !uiState.loadingBar && uiState !is SlotPickViewModel.UiState.Idle) {
        slotPickViewModel.initialize()
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        slotPickViewModel.uiEvent.collect { event ->
            when (event) {
                is SlotPickViewModel.UiEvent.NavMain -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun SlotPickTopBar(
    modifier: Modifier = Modifier,
    starPoint: Int,
    onBack: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .mainPanel()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.btn_icon_left),
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
        )
        Text(
            text = "슬롯 선택",
            fontFamily = DAL_MU_RI,
            fontSize = 20.sp,
            color = MongsWhite,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.weight(1f))
        StarPointBox(starPoint = starPoint)
    }
}

@Composable
private fun SlotPickCarousel(
    modifier: Modifier = Modifier,
    slotPickViewModel: SlotPickViewModel,
) {
    val currentMongVo by slotPickViewModel.currentMongVo.collectAsStateWithLifecycle()
    val slotIndex by slotPickViewModel.slotVoIndex.collectAsStateWithLifecycle()
    val slotVos by slotPickViewModel.slotVos.collectAsStateWithLifecycle()
    val currentSlotVo by slotPickViewModel.currentSlotVo.collectAsStateWithLifecycle()
    val starPoint by slotPickViewModel.starPoint.collectAsStateWithLifecycle()

    SelectButton(
        modifier = modifier.fillMaxSize(),
        leftBtnDisabled = slotIndex == 0,
        rightBtnDisabled = slotIndex >= slotVos.size - 1,
        leftBtnClick = slotPickViewModel::prevSlot,
        rightBtnClick = slotPickViewModel::nextSlot,
        btnWidth = SlotPickDimens.ArrowWidth,
        btnHeight = SlotPickDimens.ArrowHeight,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(SlotPickDimens.CardWidth)
                .fillMaxHeight()
                .mainPanel()
                .padding(
                    horizontal = SlotPickDimens.CardPaddingH,
                    vertical = SlotPickDimens.CardPaddingV,
                ),
        ) {
            /**
             * currentSlotVo 가 null 이 될 수 있다. 목록이 줄어들 때 ViewModel 이
             * slotVoIndex 를 클램프하지 않아서인데, wear 부터 있던 동작이라 여기서 고치지 않는다.
             * 빈 카드를 두면 좌우 화살표(min/max 로 클램프됨)로 빠져나올 수 있다.
             */
            currentSlotVo?.let { slotVo ->
                when (slotVo.type) {
                    SlotVo.SlotType.EXISTS -> slotVo.mongVo?.let {
                        Slot(
                            currentMongId = currentMongVo?.mongId,
                            mongVo = it,
                            detailDialogOpen = slotPickViewModel::detailDialogOpen,
                            graduateDialogOpen = slotPickViewModel::graduateConfirmDialogOpen,
                            deleteDialogOpen = slotPickViewModel::deleteConfirmDialogOpen,
                            pickDialogOpen = slotPickViewModel::pickConfirmDialogOpen,
                        )
                    }

                    SlotVo.SlotType.EMPTY -> EmptySlot(
                        createDialogOpen = slotPickViewModel::createDialogOpen,
                    )

                    SlotVo.SlotType.BUY -> BuySlot(
                        starPoint = starPoint,
                        buySlotPrice = BUY_SLOT_PRICE,
                        buySlotDialogOpen = slotPickViewModel::buySlotConfirmDialogOpen,
                    )
                }
            }
        }
    }
}

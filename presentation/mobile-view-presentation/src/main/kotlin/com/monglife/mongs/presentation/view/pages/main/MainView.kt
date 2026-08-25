package com.monglife.mongs.presentation.view.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.background.MainBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.dialog.common.PermissionDialog
import com.monglife.mongs.presentation.view.dialog.pages.main.InitNotificationDialog
import com.monglife.mongs.presentation.view.pages.main.component.ActionGrid
import com.monglife.mongs.presentation.view.pages.main.component.BottomBar
import com.monglife.mongs.presentation.view.pages.main.component.TopBar
import com.monglife.mongs.presentation.view.pages.main.component.InteractionRing
import com.monglife.mongs.presentation.view.pages.main.component.MongStage
import com.monglife.mongs.presentation.view.pages.main.component.SlotHeader
import com.monglife.mongs.presentation.view.pages.main.component.StatPanel
import com.monglife.mongs.presentation.view.pages.main.component.PanelDivider
import com.monglife.mongs.presentation.view.pages.main.component.mainPanel
import com.monglife.mongs.presentation.view.utils.Timer
import com.monglife.mongs.presentation.viewmodel.pages.main.MainSlotViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainStepViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Duration
import java.time.LocalDateTime

private const val HATCH_MILLIS = 5 * 60 * 1000L

/**
 * 메인 화면.
 *
 * wear 는 5분할 페이저였지만 그건 1.2인치 원형 화면의 제약이다.
 * 가로 폰은 전부 한 화면에 담을 수 있고, 그러면 페이저 위치/스와이프 중 여부/
 * 다이얼로그 생명주기라는 상태가 통째로 사라진다.
 */
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun MainView(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    mainStepViewModel: MainStepViewModel = hiltViewModel(),
) {
    /**
     * MainSlotViewModel 은 그래프 스코프를 유지한다.
     * eatingEvent() 가 Feed 화면에서 호출되기 때문이다.
     */
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainSlotViewModel: MainSlotViewModel = hiltViewModel<MainSlotViewModel>(parentEntry)

    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val currentMongVo by mainViewModel.currentMongVo.collectAsStateWithLifecycle()
    val backgroundMapCode by mainViewModel.backgroundMapCode.collectAsStateWithLifecycle()
    val starPoint by mainViewModel.starPoint.collectAsStateWithLifecycle()

    val stepVo by mainStepViewModel.stepVo.collectAsStateWithLifecycle()
    val activityPermission by mainStepViewModel.activityPermission.collectAsStateWithLifecycle()

    val slotUiState by mainSlotViewModel.uiState.collectAsStateWithLifecycle()

    var permissionDialogOpen by remember { mutableStateOf(false) }

    /**
     * 몽 중심의 화면 좌표.
     *
     * 원형 메뉴는 전체 화면 위에 떠야 하므로(무대 안에 두면 위쪽이 잘린다)
     * 무대의 위치를 재서 몽 중심을 계산한다.
     */
    var stageBounds by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current
    val mongCenterOffsetPx = with(density) { (MainDimens.GroundPadding + MainDimens.MongSize / 2).toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.loadingBar) {
            DefaultBackground()
            LoadingBar()
            return@Box
        }

        // 배경은 인셋을 무시하고 화면 끝까지 채운다.
        MainBackground(backgroundMapCode = backgroundMapCode)

        /**
         * targetSdk 35 이상은 edge-to-edge 가 강제된다.
         * 가로에서는 디스플레이 컷아웃이 좌우 한쪽을 먹으므로,
         * 조작 가능한 요소는 전부 safeDrawing 안쪽에 둔다.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 알은 경험치 대신 부화 진행률을 보여준다.
            val isEgg = currentMongVo?.level == 0
            val hatchProgress = remember { mutableFloatStateOf(0f) }

            currentMongVo?.takeIf { isEgg }?.let { mong ->
                val elapsed = Duration.between(mong.createdAt, LocalDateTime.now()).toMillis()
                Timer(
                    progress = hatchProgress,
                    startTimeMillis = elapsed,
                    maxTimeMillis = HATCH_MILLIS,
                )
            }

            TopBar(
                navController = navController,
                starPoint = starPoint,
                walkingCount = stepVo.walkingCount,
                stepAvailable = stepVo.available,
                permissionGranted = activityPermission,
                onStepClick = {
                    if (!activityPermission) {
                        permissionDialogOpen = true
                    } else {
                        navController.navigate(RouterPath.ExchangeStep.route)
                    }
                },
            )

            // 스텟 : 슬롯 : 버튼 = 2 : 3 : 2
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(MainDimens.BandGap),
            ) {
                StatPanel(
                    modifier = Modifier.weight(2f),
                    currentMongVo = currentMongVo,
                    hatchProgress = if (isEgg) ({ hatchProgress.floatValue }) else null,
                )

                Column(
                    modifier = Modifier
                        .weight(7f)
                        .fillMaxHeight()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SlotHeader(currentMongVo = currentMongVo)

                    MongStage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .onGloballyPositioned { stageBounds = it.boundsInRoot() },
                        uiState = slotUiState,
                        currentMongVo = currentMongVo,
                        onMongClick = mainSlotViewModel::interactionDialogOpen,
                        onSlotPickClick = { navController.navigate(RouterPath.SlotPick.route) },
                        onGraduateCheck = mainSlotViewModel::graduateMongCheck,
                        onEvolutionClick = mainSlotViewModel::evolutionMong,
                        onEvolutionFinish = mainSlotViewModel::evolutionMong,
                    )
                }

                Column(
                    modifier = Modifier
                        .width(MainDimens.RailWidth)
                        .fillMaxHeight()
                        .mainPanel()
                        .padding(horizontal = MainDimens.RailPadding, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    ActionGrid(navController = navController, currentMongVo = currentMongVo)

                    // 구분선은 아래 묶음에 붙여야 빈 공간에 떠 있지 않다.
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PanelDivider()
                        BottomBar(navController = navController)
                    }
                }
            }
        }

        /**
         * 첫 진입 안내.
         *
         * MainSlotViewModel 은 첫 실행 시 UiState.InitNotification 으로 들어간다.
         * 이걸 그리지 않으면 상태가 고착된다 — initialize() 는 그 상태를 벗어나기를 거부하고,
         * "다시 보지 않기" 플래그도 저장되지 않아 콜드 스타트마다 같은 상태로 재진입한다.
         *
         * 무대 안이 아니라 여기(전체 화면)에 두는 이유는 스크림이 화면 전체를 덮어야 하기 때문이다.
         */
        if (slotUiState.initNotificationDialogOpen) {
            InitNotificationDialog(
                modifier = Modifier.zIndex(3f),
                onCloseClick = mainSlotViewModel::initDialogClose,
                onCloseForeverClick = mainSlotViewModel::initDialogCloseForever,
            )
        }

        if (slotUiState.interactionDialogOpen) {
            currentMongVo?.let { mong ->
                stageBounds?.let { bounds ->
                    InteractionRing(
                        modifier = Modifier.zIndex(3f),
                        centerInRoot = Offset(
                            x = bounds.center.x,
                            y = bounds.bottom - mongCenterOffsetPx,
                        ),
                        level = mong.level,
                        stateCode = mong.stateCode,
                        isSleep = mong.isSleep,
                        onFeed = { navController.navigate(RouterPath.FeedNested.route) },
                        onStroke = { mainSlotViewModel.strokeMong(mong.mongId) },
                        onSleep = { mainSlotViewModel.sleepMong(mong.mongId) },
                        onPoopClean = { mainSlotViewModel.poopCleanMong(mong.mongId) },
                        onInventory = { navController.navigate(RouterPath.Inventory.route) },
                        onClose = mainSlotViewModel::interactionDialogClose,
                    )
                }
            }
        }

        if (permissionDialogOpen) {
            PermissionDialog(
                modifier = Modifier.zIndex(3f),
                permissionName = "활동",
                onDismiss = { permissionDialogOpen = false },
                callback = {
                    permissionDialogOpen = false
                    mainStepViewModel.verifyActivityPermission()
                },
            )
        }
    }
}

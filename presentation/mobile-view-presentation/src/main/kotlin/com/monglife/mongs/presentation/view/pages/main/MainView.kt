package com.monglife.mongs.presentation.view.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.monglife.mongs.presentation.view.pages.main.component.CareBar
import com.monglife.mongs.presentation.view.pages.main.component.ConditionPanel
import com.monglife.mongs.presentation.view.pages.main.component.MainHud
import com.monglife.mongs.presentation.view.pages.main.component.MenuRail
import com.monglife.mongs.presentation.view.pages.main.component.MongStage
import com.monglife.mongs.presentation.view.pages.main.component.SystemBar
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 알은 경험치 대신 부화 진행률을 보여준다.
            val isEgg = currentMongVo?.level == 0
            val hatchProgress = remember { mutableFloatStateOf(0f) }
            var hatchRemain by remember { mutableStateOf("") }

            currentMongVo?.takeIf { isEgg }?.let { mong ->
                val elapsed = Duration.between(mong.createdAt, LocalDateTime.now()).toMillis()
                Timer(
                    progress = hatchProgress,
                    startTimeMillis = elapsed,
                    maxTimeMillis = HATCH_MILLIS,
                )
                hatchRemain = "부화까지"
            }

            MainHud(
                level = currentMongVo?.level,
                expRatio = currentMongVo?.expRatio?.toFloat() ?: 0f,
                hatchProgress = if (isEgg) ({ hatchProgress.floatValue }) else null,
                hatchLabel = if (isEgg) hatchRemain else null,
                starPoint = starPoint,
                payPoint = currentMongVo?.payPoint,
                walkingCount = stepVo.walkingCount,
                stepAvailable = stepVo.available && activityPermission,
                onStepClick = {
                    if (!activityPermission) {
                        permissionDialogOpen = true
                    } else {
                        navController.navigate(RouterPath.ExchangeStep.route)
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(MainDimens.BandGap),
            ) {
                ConditionPanel(currentMongVo = currentMongVo)

                MongStage(
                    modifier = Modifier.weight(1f),
                    uiState = slotUiState,
                    currentMongVo = currentMongVo,
                    onMongClick = { currentMongVo?.let { mainSlotViewModel.strokeMong(it.mongId) } },
                    onSlotPickClick = { navController.navigate(RouterPath.SlotPick.route) },
                    onGraduateCheck = mainSlotViewModel::graduateMongCheck,
                    onEvolutionClick = mainSlotViewModel::evolutionMong,
                    onEvolutionFinish = mainSlotViewModel::evolutionMong,
                )

                MenuRail(navController = navController, currentMongVo = currentMongVo)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MainDimens.BottomHeight)
                    .mainPanel()
                    .padding(horizontal = MainDimens.BottomPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CareBar(
                    navController = navController,
                    currentMongVo = currentMongVo,
                    onStroke = { currentMongVo?.let { mainSlotViewModel.strokeMong(it.mongId) } },
                    onSleep = { currentMongVo?.let { mainSlotViewModel.sleepMong(it.mongId) } },
                    onPoopClean = { currentMongVo?.let { mainSlotViewModel.poopCleanMong(it.mongId) } },
                )
                Box(modifier = Modifier.weight(1f))
                SystemBar(navController = navController)
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

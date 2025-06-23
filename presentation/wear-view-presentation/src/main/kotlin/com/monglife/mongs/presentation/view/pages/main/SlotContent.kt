package com.monglife.mongs.presentation.view.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.main.slot.effect.EvolutionEffect
import com.monglife.mongs.presentation.view.component.main.slot.effect.GraduatedEffect
import com.monglife.mongs.presentation.view.component.main.slot.effect.GraduationEffect
import com.monglife.mongs.presentation.view.component.main.slot.effect.HeartEffect
import com.monglife.mongs.presentation.view.component.main.slot.effect.PoopCleanEffect
import com.monglife.mongs.presentation.view.component.main.slot.effect.PoopEffect
import com.monglife.mongs.presentation.view.component.main.slot.effect.SleepEffect
import com.monglife.mongs.presentation.view.component.main.slot.section.DeadSection
import com.monglife.mongs.presentation.view.component.main.slot.section.DeleteSection
import com.monglife.mongs.presentation.view.component.main.slot.section.EmptySection
import com.monglife.mongs.presentation.view.component.main.slot.section.GraduatedSection
import com.monglife.mongs.presentation.view.component.main.slot.section.NormalSection
import com.monglife.mongs.presentation.view.dialog.main.InitNotificationDialog
import com.monglife.mongs.presentation.view.dialog.main.InteractionDialog
import com.monglife.mongs.presentation.viewmodel.pages.main.MainPagerViewModel
import com.monglife.mongs.presentation.viewmodel.pages.main.MainSlotViewModel

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun SlotContent(
    navController: NavController,
) {
    val parentEntry = remember { navController.getBackStackEntry(RouterPath.Root.route) }
    val mainPagerViewModel: MainPagerViewModel = hiltViewModel<MainPagerViewModel>(parentEntry)
    val isPagerChange = mainPagerViewModel.isPagerChange.observeAsState(false)

    val mainSlotViewModel: MainSlotViewModel = hiltViewModel<MainSlotViewModel>(parentEntry)
    val mongVo = mainSlotViewModel.mongVo.observeAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        // content layer
        Box(modifier = Modifier.zIndex(1f)) {
            mongVo.value?.let {
                when (it.stateCode) {
                    MongStateCode.DEAD -> DeadSection(onClick = mainSlotViewModel::interactionDialogOpen)
                    MongStateCode.DELETE -> DeleteSection(
                        dialogOpen = !isPagerChange.value,
                        onClick = mainSlotViewModel::interactionDialogOpen,
                    )
                    MongStateCode.GRADUATE -> GraduatedSection(
                        mongCode = it.mongCode,
                        dialogOpen = !isPagerChange.value,
                        onClick = { navController.navigate(RouterPath.SlotPick.route) }
                    )
                    else -> {
                        if (!mainSlotViewModel.uiState.isEvolving) {
                            NormalSection(
                                mongCode = it.mongCode,
                                statusCode = it.statusCode,
                                isSleep = it.isSleep,
                                isHappy = mainSlotViewModel.uiState.isHappy,
                                isEating = mainSlotViewModel.uiState.isEating,
                                onClick = mainSlotViewModel::interactionDialogOpen,
                            )
                        }
                    }
                }
            } ?: run {
                Box(modifier = Modifier.zIndex(1f)) {
                    EmptySection {
                        navController.navigate(RouterPath.SlotPick.route)
                    }
                }
            }
        }
        // effect layer
        Box(modifier = Modifier.zIndex(2f)) {
            mongVo.value?.let {
                when (it.stateCode) {
                    MongStateCode.NORMAL -> {
                        if (mainSlotViewModel.uiState.isHappy) {
                            HeartEffect()
                        } else if (mainSlotViewModel.uiState.isPoopCleaning) {
                            PoopCleanEffect()
                        } else if (it.isSleep) {
                            SleepEffect()
                        }
                        PoopEffect(poopCount = it.poopCount)
                    }

                    MongStateCode.GRADUATE_READY -> {
                        if (!it.graduateCheck) {
                            GraduationEffect { mainSlotViewModel.graduateMongCheck(mongId = it.mongId) }
                        } else {
                            GraduatedEffect()
                        }
                    }

                    MongStateCode.EVOLUTION_READY -> {
                        if (!isPagerChange.value && !it.isSleep) {
                            EvolutionEffect(
                                mongCode = it.mongCode,
                                isEvolving = mainSlotViewModel.uiState.isEvolving,
                                onClick = mainSlotViewModel::evolutionMong,
                                callback = { mainSlotViewModel.evolutionMong(mongId = it.mongId) },
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
        // dialog layer
        Box(modifier = Modifier.zIndex(3f)) {
            mongVo.value?.let {
                if (mainSlotViewModel.uiState.interactionDialogOpen) {
                    InteractionDialog(
                        payPoint = it.payPoint,
                        level = it.level,
                        stateCode = it.stateCode,
                        isSleep = it.isSleep,
                        onInventoryClick = { navController.navigate(RouterPath.Inventory.route) },
                        onFeedClick = { navController.navigate(RouterPath.FeedNested.route) },
                        onSleepClick = { mainSlotViewModel.sleepMong(mongId = it.mongId) },
                        onPoopCleanClick = { mainSlotViewModel.poopCleanMong(mongId = it.mongId) },
                        onStrokeClick = { mainSlotViewModel.strokeMong(mongId = it.mongId) },
                        onCloseClick = mainSlotViewModel::interactionDialogClose,
                    )
                } else if (mainSlotViewModel.uiState.initNotificationDialogOpen) {
                    InitNotificationDialog(
                        onCloseClick = mainSlotViewModel::initDialogClose,
                        onCloseForeverClick = mainSlotViewModel::initDialogCloseForever
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        mainSlotViewModel.initialize()
    }
}

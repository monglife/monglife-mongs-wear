package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.component.pages.main.slot.effect.EvolutionEffect
import com.monglife.mongs.presentation.view.component.pages.main.slot.effect.GraduatedEffect
import com.monglife.mongs.presentation.view.component.pages.main.slot.effect.GraduationEffect
import com.monglife.mongs.presentation.view.component.pages.main.slot.effect.HeartEffect
import com.monglife.mongs.presentation.view.component.pages.main.slot.effect.LoadingEffect
import com.monglife.mongs.presentation.view.component.pages.main.slot.effect.PoopCleanEffect
import com.monglife.mongs.presentation.view.component.pages.main.slot.effect.PoopEffect
import com.monglife.mongs.presentation.view.component.pages.main.slot.effect.SleepEffect
import com.monglife.mongs.presentation.view.component.pages.main.slot.section.DeadSection
import com.monglife.mongs.presentation.view.component.pages.main.slot.section.DeleteSection
import com.monglife.mongs.presentation.view.component.pages.main.slot.section.EmptySection
import com.monglife.mongs.presentation.view.component.pages.main.slot.section.GraduatedSection
import com.monglife.mongs.presentation.view.component.pages.main.slot.section.NormalSection
import com.monglife.mongs.presentation.viewmodel.pages.main.MainSlotViewModel

/**
 * 몽 무대.
 *
 * wear SlotContent 의 z-layer 구조(섹션 / 이펙트)를 그대로 옮겼다.
 * 알 부화 타이머만 여기서 빠져 HUD 의 경험치 막대 자리로 갔다 — 가로 화면에서
 * 무대 한가운데에 진행 막대를 겹쳐 두면 몽을 가린다.
 *
 * wear 의 isPagerChange 가드는 페이저가 없어져 전부 상수 true 다.
 */
@Composable
internal fun MongStage(
    modifier: Modifier = Modifier,
    uiState: MainSlotViewModel.UiState,
    currentMongVo: MongVo?,
    onMongClick: () -> Unit,
    onSlotPickClick: () -> Unit,
    onGraduateCheck: (Long) -> Unit,
    onEvolutionClick: () -> Unit,
    onEvolutionFinish: (Long) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        currentMongVo?.let {
            // 캐릭터 레이어
            Box(modifier = Modifier.fillMaxSize().zIndex(1f)) {
                when (it.stateCode) {
                    MongStateCode.DEAD -> DeadSection(onClick = onMongClick)

                    MongStateCode.DELETE -> DeleteSection(
                        dialogOpen = true,
                        onClick = onMongClick,
                    )

                    MongStateCode.GRADUATE -> GraduatedSection(
                        mongCode = it.mongCode,
                        dialogOpen = true,
                        onClick = onSlotPickClick,
                    )

                    else -> {
                        if (!uiState.isEvolving) {
                            NormalSection(
                                mongCode = it.mongCode,
                                statusCode = it.statusCode,
                                isSleep = it.isSleep,
                                isHappy = uiState.isHappy,
                                isEating = uiState.isEating,
                                onClick = onMongClick,
                            )
                        }
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize().zIndex(1f)) {
                EmptySection(onClick = onSlotPickClick)
            }
        }

        // 이펙트 레이어
        Box(modifier = Modifier.fillMaxSize().zIndex(2f)) {
            currentMongVo?.let {
                when (it.stateCode) {
                    MongStateCode.NORMAL -> {
                        if (uiState.effectLoadingBar) {
                            LoadingEffect()
                        } else if (uiState.isHappy) {
                            HeartEffect()
                        } else if (uiState.isPoopCleaning) {
                            PoopCleanEffect()
                        } else if (it.isSleep) {
                            SleepEffect()
                        }
                        PoopEffect(poopCount = it.poopCount)
                    }

                    MongStateCode.GRADUATE_READY -> {
                        if (!it.graduateCheck) {
                            GraduationEffect { onGraduateCheck(it.mongId) }
                        } else {
                            GraduatedEffect()
                        }
                    }

                    MongStateCode.EVOLUTION_READY -> {
                        if (!it.isSleep) {
                            EvolutionEffect(
                                mongCode = it.mongCode,
                                isEvolving = uiState.isEvolving,
                                onClick = onEvolutionClick,
                                callback = { onEvolutionFinish(it.mongId) },
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

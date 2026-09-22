package com.monglife.mongs.presentation.view.pages.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.CircleImageButton
import com.monglife.mongs.presentation.viewmodel.pages.main.MainInteractionViewModel
import com.mongs.presentation.view.wear.R

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun InteractionContent(
    navController: NavController,
    mainInteractionViewModel: MainInteractionViewModel = hiltViewModel()
) {
    val uiState = mainInteractionViewModel.uiState.collectAsStateWithLifecycle()
    val currentMongVo = mainInteractionViewModel.currentMongVo.collectAsStateWithLifecycle()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircleImageButton(
                        icon = R.drawable.btn_icon_collection,
                        border = R.drawable.btn_border_orange,
                        iconSize = 34f,
                    ) {
                        navController.navigate(RouterPath.CollectionNested.route)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    CircleImageButton(
                        icon = R.drawable.point_icon_pay,
                        border = R.drawable.btn_border_purple_dark,
                        disable = currentMongVo.value?.let {
                            it.stateCode == MongStateCode.DEAD || it.stateCode == MongStateCode.DELETE
                        } ?: true,
                    ) {
                        navController.navigate(RouterPath.ExchangeNested.route)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    /**
                     * 미션은 몽 상태로 막지 않는다. 진행도가 계정 단위로 쌓여 몽이 없어도 볼 것이 있고,
                     * 몽이 필요한 것은 보상 수령뿐이라 상세 화면의 받기 버튼에서만 막는다.
                     */
                    CircleImageButton(
                        icon = R.drawable.btn_icon_mission,
                        border = R.drawable.btn_border_yellow,
                        iconSize = 34f,
                    ) {
                        navController.navigate(RouterPath.MissionNested.route)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    CircleImageButton(
                        icon = R.drawable.btn_icon_slot_pick,
                        border = R.drawable.btn_border_red,
                        iconSize = 34f,
                    ) {
                        navController.navigate(RouterPath.SlotPick.route)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    CircleImageButton(
                        icon = R.drawable.btn_icon_luck_draw,
                        border = R.drawable.btn_border_purple,
                        iconSize = 34f,
                        disable = currentMongVo.value?.let {
                            it.stateCode == MongStateCode.DEAD || it.stateCode == MongStateCode.DELETE
                        } ?: true,
                    ) {
                        navController.navigate(RouterPath.RandomDraw.route)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircleImageButton(
                        icon = R.drawable.btn_icon_activity,
                        border = R.drawable.btn_border_green,
                        iconSize = 34f,
                        disable = currentMongVo.value?.let {
                            it.level == 0 || it.stateCode == MongStateCode.DEAD || it.isSleep
                        } ?: true,
                    ) {
                        navController.navigate(RouterPath.TrainingNested.route)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    CircleImageButton(
                        icon = R.drawable.btn_icon_battle,
                        border = R.drawable.btn_border_pink,
                        iconSize = 30f,
                        disable = currentMongVo.value?.let {
                            it.level == 0 || it.stateCode == MongStateCode.DEAD || it.isSleep
                        } ?: true,
                    ) {
                        navController.navigate(RouterPath.BattleNested.route)
                    }
                }
            }
        }
    }
}
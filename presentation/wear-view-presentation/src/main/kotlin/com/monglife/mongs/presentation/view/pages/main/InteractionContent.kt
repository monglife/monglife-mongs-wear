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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.viewmodel.pages.main.MainInteractionViewModel
import com.monglife.mongs.presentation.wear.component.common.button.CircleImageButton
import com.mongs.wear.presentation.view.wear.R

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun InteractionContent(
    navController: NavController,
    mainInteractionViewModel: MainInteractionViewModel = hiltViewModel()
) {
    val mongVo = mainInteractionViewModel.mongVo.observeAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (mainInteractionViewModel.uiState.loadingBar) {
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
                        disable = mongVo.value?.let {
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
                    CircleImageButton(
                        icon = R.drawable.btn_icon_map_search,
                        border = R.drawable.btn_border_blue,
                        iconSize = 34f,
                    ) {
                        navController.navigate(RouterPath.SearchMap.route)
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
                        disable = mongVo.value?.let {
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
                        disable = mongVo.value?.let {
                            it.level == 0 || it.stateCode == MongStateCode.DEAD || it.isSleep
                        } ?: true,
                    ) {
                        navController.navigate(RouterPath.BattleNested.route)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        mainInteractionViewModel.initialize()
    }
}
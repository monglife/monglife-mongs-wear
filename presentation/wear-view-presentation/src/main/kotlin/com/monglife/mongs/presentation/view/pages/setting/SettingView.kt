package com.monglife.mongs.presentation.view.pages.setting

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.chip.Chip
import com.monglife.mongs.presentation.view.component.common.chip.ToggleChip
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.viewmodel.pages.setting.SettingViewModel

@Composable
internal fun SettingView(
    settingViewModel: SettingViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = settingViewModel.uiState.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        settingViewModel.verifyPermission()
    }

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                SettingContent(settingViewModel = settingViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (uiState.value.logoutConfirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "로그아웃\n하시겠습니까?",
                        confirm = settingViewModel::logout,
                        cancel = settingViewModel::logoutConfirmDialogClose
                    )
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        settingViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingViewModel.UiEvent.RequestPermission -> {
                    permissionLauncher.launch(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun SettingContent(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel,
) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 1)
    val notificationOption = settingViewModel.notificationOption.collectAsState()
    val notificationPermission = settingViewModel.notificationPermission.collectAsState()
    val activityPermission = settingViewModel.activityPermission.collectAsState()
    val locationPermission = settingViewModel.locationPermission.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        PositionIndicator(scalingLazyListState = listState)
        ScalingLazyColumn(
            contentPadding = PaddingValues(vertical = 60.dp, horizontal = 6.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState,
            autoCentering = null,
        ) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Text(
                        text = "설정",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            }

            item {
                ToggleChip(
                    fontColor = Color.White,
                    backgroundColor = Color.Black,
                    label = "알림",
                    checked = notificationOption.value && notificationPermission.value,
                    disabled = !notificationPermission.value,
                ) {
                    settingViewModel.toggleNotificationOption(notificationOption.value)
                }
            }

            item {
                Chip(
                    fontColor = Color.White,
                    backgroundColor = Color.Black,
                    label = "로그아웃",
                    secondaryLabel = "구글 계정 로그아웃",
                    onClick = settingViewModel::logoutConfirmDialogOpen,
                )
            }

            /**
             * 권한 설정
             */
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Text(
                        text = "권한",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            }

            item {
                ToggleChip(
                    fontColor = Color.White,
                    backgroundColor = Color.Black,
                    label = "알림 권한",
                    checked = notificationPermission.value,
                    disabled = false,
                ) { settingViewModel.requestNotificationPermission() }
            }

            item {
                ToggleChip(
                    fontColor = Color.White,
                    backgroundColor = Color.Black,
                    label = "활동 권한",
                    checked = activityPermission.value,
                    disabled = false,
                ) { settingViewModel.requestActivityPermission() }
            }

            item {
                ToggleChip(
                    fontColor = Color.White,
                    backgroundColor = Color.Black,
                    label = "위치 권한",
                    checked = locationPermission.value,
                    disabled = false,
                ) { settingViewModel.requestLocationPermission() }
            }
        }
    }

}
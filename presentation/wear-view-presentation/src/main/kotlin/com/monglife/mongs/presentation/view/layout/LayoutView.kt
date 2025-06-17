package com.monglife.mongs.presentation.view.layout

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.router.Router
import com.monglife.mongs.presentation.viewmodel.layout.LayoutViewModel

@Composable
fun Layout (
    context: Context = LocalContext.current,
    layoutViewModel: LayoutViewModel = hiltViewModel(),
) {

    val isLogin = layoutViewModel.isLogin.observeAsState(false)
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> layoutViewModel.verifyPermission() }

    Box {
        DefaultBackground()

        if (layoutViewModel.uiState.loadingBar) {
            LoadingBar()
        } else if (layoutViewModel.uiState.mustUpdateApp) {
            NeedUpdateContent()
        } else {
            if (!isLogin.value) {
                LoginContent()
            } else {
                Router()
            }
        }
    }

    // 권한 확인
    LaunchedEffect(Unit) {
        layoutViewModel.requestPermissionEvent.collect { permissions ->
            permissionLauncher.launch(permissions)
        }
    }

    LaunchedEffect(Unit) {
        layoutViewModel.initialize()
    }

    /**
     * 오류 메시지 표출 이벤트 (Toast)
     * @see BaseViewModel.errorEvent
     */
    LaunchedEffect(Unit) {
        BaseViewModel.errorEvent.collect { message ->
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 성공 메시지 표출 이벤트 (Toast)
     * @see BaseViewModel.successEvent
     */
    LaunchedEffect(Unit) {
        BaseViewModel.successEvent.collect { message ->
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
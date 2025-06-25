package com.monglife.mongs.presentation.view.layout

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.viewmodel.layout.LayoutViewModel

@Composable
fun LayoutView (
    context: Context = LocalContext.current,
    layoutViewModel: LayoutViewModel = hiltViewModel(),
) {
    val uiState = layoutViewModel.uiState.collectAsState()
    val isLogin = layoutViewModel.isLogin.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        layoutViewModel.verifyPermission()
    }

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else if (uiState.value.mustUpdateApp) {
            NeedUpdateContent()
        } else {
            if (!isLogin.value) {
                LoginContent()
            } else {
                Router()
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        layoutViewModel.uiEvent.collect { event ->
            when (event) {
                is LayoutViewModel.UiEvent.RequestPermission -> {
                    permissionLauncher.launch(event.permissions.toTypedArray())
                }
                else -> {}
            }
        }
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
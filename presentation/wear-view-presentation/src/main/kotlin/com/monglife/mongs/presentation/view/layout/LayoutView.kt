package com.monglife.mongs.presentation.view.layout

import android.content.Context
import android.widget.Toast
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
    layoutViewModel: LayoutViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = layoutViewModel.uiState.collectAsState()
    val isLogin = layoutViewModel.isLogin.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else if (uiState.value.mustUpdateApp) {
            NeedUpdateContent()
        } else {
            isLogin.value?.let {
                if (!it) {
                    LoginContent()
                } else {
                    Router()
                }
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
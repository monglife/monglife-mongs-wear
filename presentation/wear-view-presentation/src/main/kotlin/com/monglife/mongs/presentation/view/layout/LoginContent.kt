package com.monglife.mongs.presentation.view.layout

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.GoogleSignInButton
import com.monglife.mongs.presentation.view.component.common.logo.Logo
import com.monglife.mongs.presentation.view.utils.ViewLifeCycle
import com.monglife.mongs.presentation.viewmodel.layout.LoginViewModel

/**
 * 로그인 화면
 */
@Composable
internal fun LoginContent(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    val uiState = loginViewModel.uiState.collectAsState()
    val googleLoginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        loginViewModel::login
    )
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        loginViewModel.verifyPermissionIntentClose()
    }

    Box {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize(),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.weight(0.6f)
                ) {
                    Logo(isOpen = !uiState.value.signInButton)
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = if (uiState.value.signInButton) Alignment.Top else Alignment.CenterVertically,
                    modifier = Modifier.weight(0.4f)
                ) {
                    if (uiState.value.signInButton) {
                        GoogleSignInButton(onClick = {
                            loginViewModel.googleLogin(
                                googleLoginLauncher = googleLoginLauncher
                            )
                        })
                    }

                    if (uiState.value.loadingBar) {
                        LoadingBar()
                    }
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        loginViewModel.uiEvent.collect { event ->
            when (event) {
                is LoginViewModel.UiEvent.RequestPermission -> {
                    permissionLauncher.launch(event.permissions.toTypedArray())
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        loginViewModel.initialize()
    }

    ViewLifeCycle(
        lifecycleOwner = lifecycleOwner,
        onCreate = loginViewModel::verifyPermissionIntentOpen
    )
}

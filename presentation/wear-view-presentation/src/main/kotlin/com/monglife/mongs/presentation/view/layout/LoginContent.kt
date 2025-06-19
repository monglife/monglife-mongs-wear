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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.GoogleSignInButton
import com.monglife.mongs.presentation.view.component.common.logo.Logo
import com.monglife.mongs.presentation.viewmodel.layout.LoginViewModel

/**
 * 로그인 화면
 */
@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    val googleLoginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        loginViewModel::login
    )

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
                    Logo(isOpen = !loginViewModel.uiState.signInButton)
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = if (loginViewModel.uiState.signInButton) Alignment.Top else Alignment.CenterVertically,
                    modifier = Modifier.weight(0.4f)
                ) {
                    if (loginViewModel.uiState.signInButton) {
                        GoogleSignInButton(onClick = {
                            loginViewModel.googleLogin(
                                googleLoginLauncher = googleLoginLauncher
                            )
                        })
                    }

                    if (loginViewModel.uiState.loadingBar) {
                        LoadingBar()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loginViewModel.initialize()
    }
}

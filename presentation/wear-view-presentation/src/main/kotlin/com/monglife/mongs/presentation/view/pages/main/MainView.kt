package com.monglife.mongs.presentation.view.pages.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.viewmodel.pages.main.MainViewModel

@Composable
fun MainView(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    Box {
        DefaultBackground()

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            BlueButton(text = "로그아웃", onClick = mainViewModel::logout)
        }
    }
}


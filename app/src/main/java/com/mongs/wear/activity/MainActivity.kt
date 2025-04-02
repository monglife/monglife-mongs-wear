package com.mongs.wear.activity

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mongs.wear.domain.device.usecase.SetDeviceIdUseCase
import com.mongs.wear.presentation.assets.MongsTheme
import com.mongs.wear.presentation.layout.MainView
import com.mongs.wear.viewModel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱 초기화
        mainActivityViewModel.initApp()

        /**
         * UI 로딩
         */
        setContent {
            MongsTheme {
                MainView(initPending = mainActivityViewModel.uiState.initPending)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 브로커 연결 + 플레이어 정보 동기화 + 현재 몽 정보 동기화 + 걸음 수 동기화
        mainActivityViewModel.resumeApp()
    }

    override fun onPause() {
        // 브로커 연결 해제 + 구독 해제
        mainActivityViewModel.pauseMqtt()
        super.onPause()
    }

    override fun onDestroy() {
        // 브로커 연결 해제 + 구독 해제
        mainActivityViewModel.disConnectMqtt()
        super.onDestroy()
    }
}
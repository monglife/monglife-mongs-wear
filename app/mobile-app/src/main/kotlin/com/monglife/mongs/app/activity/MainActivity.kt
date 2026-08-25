package com.monglife.mongs.app.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.presentation.view.assets.MongsTheme
import com.monglife.mongs.presentation.view.layout.LayoutView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mqttClient: MqttClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * 전체 화면.
         *
         * 가로 고정 픽셀 게임이라 상태바와 제스처바가 화면을 먹을 이유가 없다.
         * 스와이프하면 잠깐 나타났다 다시 숨는다.
         *
         * safeDrawing 패딩은 그대로 둔다. 인셋이 0 으로 줄 뿐이고,
         * 컷아웃과 일시적으로 나타나는 바에 대한 안전망으로 남는다.
         */
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        /**
         * UI 로딩
         */
        setContent {
            MongsTheme {
                LayoutView()
            }
        }
    }

    override fun onDestroy() {
        CoroutineScope(Dispatchers.Main).launch {
            mqttClient.disconnect()
        }
        super.onDestroy()
    }
}
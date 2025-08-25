package com.monglife.mongs.app.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
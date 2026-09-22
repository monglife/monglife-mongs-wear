package com.monglife.mongs.app.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.presentation.view.assets.MongsTheme
import com.monglife.mongs.presentation.view.layout.LayoutView
import dagger.hilt.android.AndroidEntryPoint
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

    /**
     * 앱을 정말 나갈 때만 연결을 끊는다.
     *
     * <p>{@code isFinishing} 을 보는 이유는 화면 재생성 때문이다. 재생성에서는 새 액티비티의
     * {@code onCreate} 가 옛 액티비티의 {@code onDestroy} 보다 먼저 올 수 있는데, 그때 해제를
     * 걸면 방금 붙은 연결을 도로 끊는다.
     *
     * <p>해제 자체는 {@link MqttClient} 안의 스코프에서 돈다. 액티비티가 스코프를 직접 만들면
     * 그 스코프는 아무도 취소하지 않고, 연결·구독과 겹치는 순서도 통제할 수 없다.
     */
    override fun onDestroy() {
        if (isFinishing) {
            mqttClient.disconnectAsync()
        }
        super.onDestroy()
    }
}
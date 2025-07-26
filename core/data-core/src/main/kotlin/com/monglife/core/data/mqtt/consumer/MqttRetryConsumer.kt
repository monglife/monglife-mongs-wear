package com.monglife.core.data.mqtt.consumer

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttRetryConsumer @Inject constructor(
    private val callbackMap: Map<String, MqttCallback>,
    private val onConnectLost: () -> Unit,
) : MqttCallback {

    companion object {
        private const val TAG = "MqttRetryConsumer"
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun connectionLost(cause: Throwable?) {
        applicationScope.launch {
            runCatching {
                if (callbackMap.isNotEmpty()) {
                    onConnectLost()

                    val out = StringBuilder()
                        .append("연결 중단")
                        .append("\n")

                    callbackMap.keys.forEach { topic ->
                        out.append("  - topic         => $topic\n")
                    }

                    Log.i(TAG, "MQTT >> $out")
                }
            }
        }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {}

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
}
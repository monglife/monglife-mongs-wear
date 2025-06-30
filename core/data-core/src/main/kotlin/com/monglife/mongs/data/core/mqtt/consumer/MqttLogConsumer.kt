package com.monglife.mongs.data.core.mqtt.consumer

import android.util.Log
import com.google.gson.Gson
import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttLogConsumer @Inject constructor(
    private val gson: Gson,
) : MqttCallback {

    companion object {
        private const val TAG = "MqttLogConsumer"
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        message?.let {
            topic?.let {
                runCatching {
                    val out = StringBuilder()
                        .append("%-30s".format(topic))
                        .append("\n")

                    val bodyJson = message.toString()

                    gson.fromJson(bodyJson, ResponseDto::class.java).let { responseDto ->
                        out
                            .append("  - http status   => ${responseDto?.httpStatus}\n")
                            .append("  - response code => ${responseDto?.code}\n")
                            .append("  - message       => ${responseDto?.message}\n")
                            .append("  - result        => ${responseDto?.result}")
                    }

                    Log.i(TAG, "MQTT >> $out")
                }
            }
        }
    }

    override fun connectionLost(cause: Throwable?) {}

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
}
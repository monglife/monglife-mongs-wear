package com.monglife.core.data.mqtt.consumer

import android.util.Log
import com.monglife.core.data.mqtt.utils.MqttUtil
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttLogConsumer @Inject constructor(
    private val mqttUtil: MqttUtil,
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

                    mqttUtil.fromJson(mqttMessage = message, classType = Map::class.java)
                        .let { responseDto ->
                            out
                                .append("  - http status   => ${responseDto.httpStatus}\n")
                                .append("  - response code => ${responseDto.code}\n")
                                .append("  - message       => ${responseDto.message}\n")
                                .append("  - result        => ${responseDto.result}")
                        }

                    Log.i(TAG, "MQTT >> $out")
                }.onFailure {
                    Log.e(TAG, "MQTT >> ${it.stackTraceToString()}")
                }
            }
        }
    }

    override fun connectionLost(cause: Throwable?) {}

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
}
package com.monglife.core.data.mqtt.consumer

import android.util.Log
import com.google.gson.Gson
import com.monglife.core.data.web.dto.response.ResponseDto
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

    override fun connectionLost(cause: Throwable?) {}

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        message?.let {
            topic?.let {
                runCatching {
                    val out = StringBuilder()
                        .append("%-30s".format(topic))
                        .append("\n")

                    message.fromJson(classType = Map::class.java)
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

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}

    private fun <T> MqttMessage.fromJson(classType: Class<T>): ResponseDto<T> =
        gson.fromJson(this.toString(), ResponseDto::class.java).let { responseDto ->
            ResponseDto(
                httpStatus = responseDto.httpStatus,
                code = responseDto.code,
                message = responseDto.message,
                result = gson.fromJson(gson.toJson(responseDto.result), classType),
            )
        }
}
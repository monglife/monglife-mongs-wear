package com.monglife.core.data.mqtt.consumer

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.monglife.core.data.global.isDebuggable
import com.monglife.core.data.web.dto.response.ResponseDto
import dagger.hilt.android.qualifiers.ApplicationContext
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttLogConsumer @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson,
) : MqttCallback {

    companion object {
        private const val TAG = "MqttLogConsumer"
    }

    /**
     * 이 소비자는 MQTT 응답 바디 전문을 로그로 남긴다.
     * 몽 상태 · 걸음 수 · 별포인트 · 배틀 정보가 그대로 찍히므로 릴리스에서는 실행하지 않는다.
     */
    private val isDebuggable = context.isDebuggable()

    override fun connectionLost(cause: Throwable?) {}

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        // Gson 파싱보다 먼저 끊는다. 파싱 자체가 메시지마다 발생하는 낭비다.
        if (!isDebuggable) return

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
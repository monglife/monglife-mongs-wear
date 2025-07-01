package com.monglife.mongs.data.core.mqtt.utils

import com.google.gson.Gson
import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttUtil @Inject constructor(
    private val gson: Gson,
) {

    fun <T> fromJson(mqttMessage: MqttMessage, classType: Class<T>): ResponseDto<T> =
        gson.fromJson(mqttMessage.toString(), ResponseDto::class.java).let { responseDto ->
            ResponseDto(
                httpStatus = responseDto.httpStatus,
                code = responseDto.code,
                message = responseDto.message,
                result = gson.fromJson(gson.toJson(responseDto.result), classType),
            )
    }
}
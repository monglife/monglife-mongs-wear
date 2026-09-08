package com.monglife.core.data.mqtt.consumer

import com.google.gson.Gson
import com.monglife.core.data.web.dto.response.ResponseDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.reflect.Type

class MqttConsumer<T>(
    private val topic: String,
    private val classType: Class<T>,
    private val gson: Gson,
    private val onReceive: suspend (ResponseDto<T>) -> Unit,
) : MqttCallback {

    private val mutex = Mutex()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun connectionLost(cause: Throwable?) {}

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        applicationScope.launch {
            runCatching {
                if (message == null || topic == null) return@launch
                if (topic != this@MqttConsumer.topic) return@launch
                mutex.withLock {
                    onReceive(message.fromJson(type = classType))
                }
            }
        }
    }

    private fun <T> MqttMessage.fromJson(type: Type): ResponseDto<T> =
        gson.fromJson(this.toString(), ResponseDto::class.java).let { responseDto ->
            ResponseDto(
                httpStatus = responseDto.httpStatus,
                code = responseDto.code,
                message = responseDto.message,
                result = gson.fromJson(gson.toJson(responseDto.result), type),
            )
        }
}
package com.monglife.core.data.mqtt.client

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.monglife.core.data.mqtt.consumer.MqttLogConsumer
import com.monglife.core.data.mqtt.exception.InvalidConnectException
import com.monglife.core.data.mqtt.exception.InvalidDisConnectException
import com.monglife.core.data.mqtt.exception.InvalidDisSubscribeException
import com.monglife.core.data.mqtt.exception.InvalidPublishException
import com.monglife.core.data.mqtt.exception.InvalidSubscribeException
import com.monglife.core.data.mqtt.exception.UnKnownException
import com.mongs.wear.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MqttClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttAndroidClient: MqttAndroidClient,
    private val mqttLogConsumer: MqttLogConsumer,
    private val gson: Gson,
) {

    companion object {
        private const val TAG = "MqttClient"

        private sealed class MqttUserContext {
            data object Connect : MqttUserContext()
            data object Disconnect : MqttUserContext()
            data class Subscribe(val topic: String) : MqttUserContext()
            data class DisSubscribe(val topic: String) : MqttUserContext()
            data class Publish(val topic: String, val payload: String) : MqttUserContext()
        }
    }

    private val mqttConnectionMutex = Mutex()
    private val callbackMap = ConcurrentHashMap<String, MqttCallback>()

    /**
     * Mqtt 브로커 연결
     */
    private suspend fun connect(): MqttAndroidClient = mqttConnectionMutex.withLock {
        if (!mqttAndroidClient.isConnected) {
            val options = MqttConnectOptions().apply {
                this.userName = context.getString(R.string.mongs_mqtt_username)
                this.password = context.getString(R.string.mongs_mqtt_password).toCharArray()
                this.keepAliveInterval = context.getString(R.string.mongs_mqtt_keep_alive).toInt()
                this.isCleanSession = true
            }

            mqttAndroidClient.setCallback(mqttLogConsumer)
            mqttAndroidClient.connect(
                options = options,
                userContext = MqttUserContext.Connect,
                callback = null
            ).await()
        }

        return mqttAndroidClient.takeIf { it.isConnected } ?: throw InvalidConnectException()
    }

    /**
     * Mqtt 메시지 전송
     */
    suspend fun <T> publish(topic: String, requestDto: T) {
        val mqttAndroidClient = connect()

        val payload = gson.toJson(requestDto)

        mqttAndroidClient.publish(
            topic = topic,
            payload = payload.toByteArray(),
            qos = 2,
            retained = false,
            userContext = MqttUserContext.Publish(topic = topic, payload = payload),
            callback = null
        ).await()
    }

    /**
     * Mqtt 구독
     */
    suspend fun subscribe(topic: String, callback: MqttCallback) {
        val mqttAndroidClient = connect()

        mqttAndroidClient.addCallback(callback = callback)
        mqttAndroidClient.subscribe(
            topic = topic,
            qos = 2,
            userContext = MqttUserContext.Subscribe(topic = topic),
            callback = null
        ).await()

        callbackMap[topic] = callback
    }

    /**
     * Mqtt 구독 해제
     */
    suspend fun disSubscribe(topic: String) {
        // 연결 중인 경우만 구독 해제
        runCatching {
            this.mqttAndroidClient.takeIf { it.isConnected }?.let {
                mqttAndroidClient.unsubscribe(
                    topic = topic,
                    userContext = MqttUserContext.DisSubscribe(topic = topic),
                    callback = null
                ).await()

                callbackMap[topic]?.let {
                    mqttAndroidClient.removeCallback(callback = it)
                    callbackMap.remove(topic)
                }
            }
        }
    }

    /**
     * Mqtt 연결 해제
     */
    fun disconnect() {
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.disconnect(
                userContext = MqttUserContext.Disconnect,
                callback = null
            )
        }
    }

    private suspend fun IMqttToken.await() = suspendCoroutine { cont ->
        this.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                asyncActionToken?.let {
                    asyncActionToken.userContext?.let {
                        val out = StringBuilder()

                        when (asyncActionToken.userContext) {
                            is MqttUserContext.Connect -> out
                                .append("연결")
                            is MqttUserContext.Disconnect -> out
                                .append("연결 해제")
                            is MqttUserContext.Subscribe -> out
                                .append("토픽 구독\n")
                                .append("  - topic         => ${(asyncActionToken.userContext as MqttUserContext.Subscribe).topic}")
                            is MqttUserContext.DisSubscribe -> out
                                .append("토픽 구독 해제\n")
                                .append("  - topic         => ${(asyncActionToken.userContext as MqttUserContext.DisSubscribe).topic}")
                            is MqttUserContext.Publish -> out
                                .append("메시지 전송\n")
                                .append("  - topic         => ${(asyncActionToken.userContext as MqttUserContext.Publish).topic}")
                                .append("  - payload       => ${(asyncActionToken.userContext as MqttUserContext.Publish).payload}")
                        }

                        if (out.isNotBlank()) {
                            Log.i(TAG, "MQTT >> $out")
                        }
                    }
                }

                cont.resume(Unit)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                asyncActionToken?.let {
                    asyncActionToken.userContext?.let {
                        val out = StringBuilder()

                        when (asyncActionToken.userContext) {
                            is MqttUserContext.Connect -> {
                                out
                                    .append("연결 실패")
                                    .append("  - exception     => ${exception?.stackTraceToString() ?: ""}")

                                cont.resumeWithException(InvalidConnectException())
                            }
                            is MqttUserContext.Disconnect -> {
                                out
                                    .append("연결 해제 실패")
                                    .append("  - exception     => ${exception?.stackTraceToString() ?: ""}")

                                cont.resumeWithException(InvalidDisConnectException())
                            }
                            is MqttUserContext.Subscribe -> {
                                out
                                    .append("토픽 구독 실패\n")
                                    .append("  - topic         => ${(asyncActionToken.userContext as MqttUserContext.Subscribe).topic}")
                                    .append("  - exception     => ${exception?.stackTraceToString() ?: ""}")

                                cont.resumeWithException(InvalidSubscribeException())
                            }
                            is MqttUserContext.DisSubscribe -> {
                                out
                                    .append("토픽 구독 해제 실패\n")
                                    .append("  - topic         => ${(asyncActionToken.userContext as MqttUserContext.DisSubscribe).topic}")
                                    .append("  - exception     => ${exception?.stackTraceToString() ?: ""}")

                                cont.resumeWithException(InvalidDisSubscribeException())
                            }
                            is MqttUserContext.Publish -> {
                                out
                                    .append("메시지 전송 실패\n")
                                    .append("  - topic         => ${(asyncActionToken.userContext as MqttUserContext.Publish).topic}")
                                    .append("  - payload       => ${(asyncActionToken.userContext as MqttUserContext.Publish).payload}")
                                    .append("  - exception     => ${exception?.stackTraceToString() ?: ""}")

                                cont.resumeWithException(InvalidPublishException())
                            }

                            else -> {
                                out
                                    .append("알 수 없는 예외\n")
                                    .append("  - exception     => ${exception?.stackTraceToString() ?: ""}")

                                cont.resumeWithException(exception ?: UnKnownException())
                            }
                        }

                        if (out.isNotBlank()) {
                            Log.w(TAG, "MQTT >> $out")
                        }
                    }
                }
            }
        }
    }
}
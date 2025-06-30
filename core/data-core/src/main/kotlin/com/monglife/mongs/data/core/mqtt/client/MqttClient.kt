package com.monglife.mongs.data.core.mqtt.client

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.monglife.mongs.data.core.mqtt.consumer.MqttLogConsumer
import com.monglife.mongs.data.core.mqtt.exception.InvalidConnectException
import com.monglife.mongs.data.core.mqtt.exception.InvalidDisConnectException
import com.monglife.mongs.data.core.mqtt.exception.InvalidDisSubscribeException
import com.monglife.mongs.data.core.mqtt.exception.InvalidPublishException
import com.monglife.mongs.data.core.mqtt.exception.InvalidSubscribeException
import com.monglife.mongs.data.core.mqtt.exception.UnKnownException
import com.mongs.wear.data.core.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Module
@InstallIn(SingletonComponent::class)
object MqttClientModule {
    @Provides
    @Singleton
    fun provideMqttClient(@ApplicationContext context: Context): MqttAndroidClient =
        MqttAndroidClient(
            context = context,
            serverURI = context.getString(R.string.mongs_mqtt_url),
            clientId = MqttClient.generateClientId()
        )
}

class MqttClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttAndroidClient: MqttAndroidClient,
    private val mqttLogConsumer: MqttLogConsumer,
    private val gson: Gson,
) {

    companion object {
        private const val TAG = "MqttClient"

        private enum class MqttUserContext {
            CONNECT,
            DISCONNECT,
            SUBSCRIBE,
            DIS_SUBSCRIBE,
            PUBLISH,
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
                userContext = MqttUserContext.CONNECT,
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
            userContext = MqttUserContext.PUBLISH,
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
            userContext = MqttUserContext.SUBSCRIBE,
            callback = null
        ).await()

        callbackMap[topic] = callback
    }

    /**
     * Mqtt 구독 해제
     */
    suspend fun disSubscribe(topic: String) {
        // 연결 중인 경우만 구독 해제
        this.mqttAndroidClient.takeIf { it.isConnected }?.let {
            mqttAndroidClient.unsubscribe(
                topic = topic,
                userContext = MqttUserContext.DIS_SUBSCRIBE,
                callback = null
            ).await()
        }.also {
            callbackMap[topic]?.let {
                mqttAndroidClient.removeCallback(callback = it)
                callbackMap.remove(topic)
            }
        }
    }

    /**
     * Mqtt 연결 해제
     */
    suspend fun disconnect() {
        mqttConnectionMutex.withLock {
            if (mqttAndroidClient.isConnected) {
                mqttAndroidClient.disconnect(
                    userContext = MqttUserContext.DISCONNECT,
                    callback = null
                ).await()
            }
        }
    }

    private suspend fun IMqttToken.await() = suspendCancellableCoroutine { cont ->
        this.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                asyncActionToken?.let {
                    asyncActionToken.userContext?.let {
                        when (asyncActionToken.userContext) {
                            MqttUserContext.CONNECT -> Log.i(
                                TAG,
                                "연결 성공 | isConnected: ${mqttAndroidClient.isConnected}"
                            )

                            MqttUserContext.DISCONNECT -> Log.i(
                                TAG,
                                "연결 해제 성공 | isConnected: ${mqttAndroidClient.isConnected}"
                            )

                            MqttUserContext.SUBSCRIBE -> Log.i(
                                TAG,
                                "구독 성공 | topic: ${asyncActionToken.topics.contentToString()}"
                            )

                            MqttUserContext.DIS_SUBSCRIBE -> Log.i(
                                TAG,
                                "구독 해제 성공 | topic: ${asyncActionToken.topics.contentToString()}"
                            )

                            MqttUserContext.PUBLISH -> Log.i(
                                TAG,
                                "전송 성공 | topic: ${asyncActionToken.topics.contentToString()}"
                            )

                            else -> {}
                        }
                    }
                }

                cont.resume(Unit)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                asyncActionToken?.let {
                    asyncActionToken.userContext?.let {
                        when (asyncActionToken.userContext) {
                            MqttUserContext.CONNECT -> {
                                Log.i(TAG, "연결 실패 | isConnected: ${mqttAndroidClient.isConnected}")
                                cont.resumeWithException(InvalidConnectException())
                            }

                            MqttUserContext.DISCONNECT -> {
                                Log.i(
                                    TAG,
                                    "연결 해제 실패 | isConnected: ${mqttAndroidClient.isConnected}"
                                )
                                cont.resumeWithException(InvalidDisConnectException())
                            }

                            MqttUserContext.SUBSCRIBE -> {
                                Log.i(
                                    TAG,
                                    "구독 실패 | topic: ${asyncActionToken.topics.contentToString()}"
                                )
                                cont.resumeWithException(InvalidSubscribeException())
                            }

                            MqttUserContext.DIS_SUBSCRIBE -> {
                                Log.i(
                                    TAG,
                                    "구독 해제 실패 | topic: ${asyncActionToken.topics.contentToString()}"
                                )
                                cont.resumeWithException(InvalidDisSubscribeException())
                            }

                            MqttUserContext.PUBLISH -> {
                                Log.i(
                                    TAG,
                                    "전송 실패 | topic: ${asyncActionToken.topics.contentToString()}"
                                )
                                cont.resumeWithException(InvalidPublishException())
                            }

                            else -> cont.resumeWithException(exception ?: UnKnownException())
                        }
                    }
                }
            }
        }
    }
}
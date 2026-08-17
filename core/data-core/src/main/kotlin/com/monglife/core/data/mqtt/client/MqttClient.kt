package com.monglife.core.data.mqtt.client

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.monglife.core.data.global.isDebuggable
import com.monglife.core.data.mqtt.consumer.MqttConsumer
import com.monglife.core.data.mqtt.consumer.MqttLogConsumer
import com.monglife.core.data.mqtt.consumer.MqttRetryConsumer
import com.monglife.core.data.mqtt.exception.InvalidConnectException
import com.monglife.core.data.mqtt.exception.InvalidDisConnectException
import com.monglife.core.data.mqtt.exception.InvalidDisSubscribeException
import com.monglife.core.data.mqtt.exception.InvalidPublishException
import com.monglife.core.data.mqtt.exception.InvalidSubscribeException
import com.monglife.core.data.mqtt.exception.UnKnownException
import com.monglife.core.data.web.dto.response.ResponseDto
import com.mongs.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.pow

@Singleton
class MqttClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttAndroidClient: MqttAndroidClient,
    private val mqttLogConsumer: MqttLogConsumer,
    private val gson: Gson,
) {

    companion object {
        private const val TAG = "MqttClient"

        private const val SUBSCRIBE_RETRY_DELAY = 10000L
        private const val SUBSCRIBE_RETRY_MAX_DELAY = 300000L
        private const val SUBSCRIBE_RETRY_MAX_ATTEMPT = 10

        private sealed class MqttUserContext {
            data object Connect : MqttUserContext()
            data object Disconnect : MqttUserContext()
            data class Subscribe(val topic: String) : MqttUserContext()
            data class DisSubscribe(val topic: String) : MqttUserContext()
            data class Publish(val topic: String, val payload: String) : MqttUserContext()
        }
    }

    /**
     * 성공 로그는 publish payload 전문을 포함하므로 릴리스에서는 남기지 않는다.
     */
    private val isDebuggable = context.isDebuggable()

    private val mutex = Mutex()
    private val callbackMap = ConcurrentHashMap<String, MqttCallback>()
    private val retrySubscribeJobMap = ConcurrentHashMap<String, Job>()

    /**
     * 구독 재시도 전용 스코프
     * 재시도 코루틴을 여기에 묶어 두어야 disconnect 시 일괄 취소할 수 있다.
     */
    private val retryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 연결 중단 감지 소비자
     * connect 마다 새로 만들면 스코프가 누적되므로 인스턴스를 재사용한다.
     */
    private val mqttRetryConsumer by lazy {
        MqttRetryConsumer(
            callbackMap = callbackMap,
            onConnectLost = {
                callbackMap.forEach { (topic, callback) ->
                    scheduleRetrySubscribe(topic = topic, callback = callback)
                }
            }
        )
    }

    /**
     * Mqtt 브로커 연결
     */
    @Throws(InvalidConnectException::class)
    private suspend fun connect(): MqttAndroidClient = mutex.withLock {
        if (!mqttAndroidClient.isConnected) {
            val options = MqttConnectOptions().apply {
                this.userName = context.getString(R.string.mongs_mqtt_username)
                this.password = context.getString(R.string.mongs_mqtt_password).toCharArray()
                this.keepAliveInterval = context.getString(R.string.mongs_mqtt_keep_alive).toInt()
                this.isCleanSession = true
            }

            /**
             * setCallback 은 라이브러리 내부 callbacksList 를 clear 한 뒤 add 한다.
             * 따라서 재연결 때마다 토픽별 소비자가 통째로 사라진다.
             * 여기서는 그 동작을 "초기화" 로 의도적으로 활용하고,
             * 기준 소비자와 토픽별 소비자를 곧바로 다시 등록해 준다.
             */
            mqttAndroidClient.setCallback(mqttLogConsumer)
            mqttAndroidClient.addCallback(mqttRetryConsumer)
            callbackMap.values.forEach { mqttAndroidClient.addCallback(callback = it) }

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
    suspend fun <T> subscribe(
        topic: String,
        classType: Class<T>,
        onReceive: suspend (ResponseDto<T>) -> Unit
    ) {
        // 이미 구독 중이거나 재시도 중인 토픽은 중복 등록하지 않는다.
        if (callbackMap.containsKey(topic) || retrySubscribeJobMap.containsKey(topic)) return

        val callback = MqttConsumer(
            topic = topic,
            onReceive = onReceive,
            classType = classType,
            gson = gson,
        )

        runCatching {
            subscribeInternal(topic = topic, callback = callback)
        }.onFailure {
            Log.w(TAG, "MQTT >> 토픽 구독 실패, 재시도 예약\n  - topic         => $topic", it)

            scheduleRetrySubscribe(topic = topic, callback = callback)
        }
    }

    /**
     * Mqtt 구독 재시도 예약
     * Job 을 map 에 먼저 등록한 뒤 시작해야 등록 전 완료로 인한 유령 엔트리가 생기지 않는다.
     */
    private fun scheduleRetrySubscribe(topic: String, callback: MqttCallback) {
        val job = retryScope.launch(start = CoroutineStart.LAZY) {
            try {
                var attempt = 0

                while (isActive && attempt < SUBSCRIBE_RETRY_MAX_ATTEMPT) {
                    attempt++

                    delay(retryDelayMillis(attempt = attempt))

                    val result = runCatching {
                        subscribeInternal(topic = topic, callback = callback)
                    }

                    if (result.isSuccess) return@launch

                    Log.w(
                        TAG,
                        "MQTT >> 토픽 구독 재시도 실패 ($attempt/$SUBSCRIBE_RETRY_MAX_ATTEMPT)\n  - topic         => $topic",
                        result.exceptionOrNull()
                    )
                }

                Log.e(TAG, "MQTT >> 토픽 구독 재시도 포기\n  - topic         => $topic")
            } finally {
                retrySubscribeJobMap.remove(topic)
            }
        }

        if (retrySubscribeJobMap.putIfAbsent(topic, job) != null) {
            // 이미 재시도가 진행 중
            job.cancel()
            return
        }

        job.start()
    }

    private fun retryDelayMillis(attempt: Int): Long =
        (SUBSCRIBE_RETRY_DELAY * 2.0.pow(attempt - 1))
            .toLong()
            .coerceAtMost(SUBSCRIBE_RETRY_MAX_DELAY)

    private suspend fun subscribeInternal(topic: String, callback: MqttCallback) {
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
        runCatching {
            retrySubscribeJobMap.remove(topic)?.cancel()

            // 연결 중인 경우만 구독 해제
            this.mqttAndroidClient.takeIf { it.isConnected }?.let {
                mqttAndroidClient.unsubscribe(
                    topic = topic,
                    userContext = MqttUserContext.DisSubscribe(topic = topic),
                    callback = null
                ).await()
            }

            callbackMap.remove(topic)?.let {
                mqttAndroidClient.removeCallback(callback = it)
            }
        }.onFailure {
            Log.w(TAG, "MQTT >> 토픽 구독 해제 실패\n  - topic         => $topic", it)
        }
    }

    /**
     * Mqtt 연결 해제
     */
    suspend fun disconnect() {
        runCatching {
            retrySubscribeJobMap.values.forEach { it.cancel() }
            retrySubscribeJobMap.clear()

            // 라이브러리 내부 callbacksList 에서도 제거해야 소비자가 남지 않는다.
            callbackMap.values.forEach { mqttAndroidClient.removeCallback(callback = it) }
            callbackMap.clear()

            if (mqttAndroidClient.isConnected) {
                mqttAndroidClient.disconnect(
                    userContext = MqttUserContext.Disconnect,
                    callback = null
                ).await()
            }
        }.onFailure {
            Log.w(TAG, "MQTT >> 연결 해제 실패", it)
        }
    }

    private suspend fun IMqttToken.await() = suspendCancellableCoroutine { cont ->
        this.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                logSuccess(userContext = asyncActionToken?.userContext)

                cont.resume(Unit)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                val failure = logFailureAndResolve(
                    userContext = asyncActionToken?.userContext,
                    exception = exception,
                )

                // userContext 가 null 이어도 반드시 재개해야 호출자가 영구 정지하지 않는다.
                cont.resumeWithException(failure)
            }
        }
    }

    private fun logSuccess(userContext: Any?) {
        if (!isDebuggable) return

        val out = when (userContext) {
            is MqttUserContext.Connect -> "연결"

            is MqttUserContext.Disconnect -> "연결 해제"

            is MqttUserContext.Subscribe ->
                "토픽 구독\n  - topic         => ${userContext.topic}"

            is MqttUserContext.DisSubscribe ->
                "토픽 구독 해제\n  - topic         => ${userContext.topic}"

            is MqttUserContext.Publish ->
                "메시지 전송\n" +
                    "  - topic         => ${userContext.topic}\n" +
                    "  - payload       => ${userContext.payload}"

            else -> return
        }

        Log.i(TAG, "MQTT >> $out")
    }

    private fun logFailureAndResolve(userContext: Any?, exception: Throwable?): Throwable {
        val (out, failure) = when (userContext) {
            is MqttUserContext.Connect ->
                "연결 실패" to InvalidConnectException()

            is MqttUserContext.Disconnect ->
                "연결 해제 실패" to InvalidDisConnectException()

            is MqttUserContext.Subscribe ->
                "토픽 구독 실패\n  - topic         => ${userContext.topic}" to InvalidSubscribeException()

            is MqttUserContext.DisSubscribe ->
                "토픽 구독 해제 실패\n  - topic         => ${userContext.topic}" to InvalidDisSubscribeException()

            is MqttUserContext.Publish ->
                "메시지 전송 실패\n" +
                    "  - topic         => ${userContext.topic}\n" +
                    "  - payload       => ${userContext.payload}" to InvalidPublishException()

            else ->
                "알 수 없는 예외" to (exception ?: UnKnownException())
        }

        Log.w(
            TAG,
            "MQTT >> $out\n  - exception     => ${exception?.stackTraceToString() ?: ""}"
        )

        return failure
    }
}

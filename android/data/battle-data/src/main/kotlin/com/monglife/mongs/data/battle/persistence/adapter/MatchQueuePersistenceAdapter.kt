package com.monglife.mongs.data.battle.persistence.adapter

import android.content.Context
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.battle.port.persistence.MatchQueuePersistencePort
import com.monglife.mongs.data.battle.persistence.dto.MatchQueueEventDto
import com.monglife.mongs.domain.battle.model.MatchQueue
import com.mongs.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class MatchQueuePersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttClient: MqttClient,
) : MatchQueuePersistencePort {

    /**
     * 매칭 이벤트 1건. deviceId 토픽으로 오는 것이라 mongId 는 실려 있지 않다.
     */
    private data class MatchedEvent(val matchId: Long, val playerId: String)

    /**
     * deviceId 별 구독 카운터. 토픽이 deviceId 단위이므로 키도 deviceId 다.
     * (mongId 로 세던 것을 맞췄다 - 토픽과 키가 어긋나 있었다.)
     */
    private val subscribeCounterMap = ConcurrentHashMap<String, AtomicInteger>()

    /**
     * deviceId 별 매칭 이벤트 버스.
     *
     * <b>MQTT 콜백이 쓰는 대상은 어댑터가 소유해야 한다.</b> 예전에는 flow 본문에서 만든
     * 지역 StateFlow 에 콜백을 묶었는데, 중복 구독 가드 때문에 두 번째 구독은 subscribe 를
     * 건너뛴다. 그러면 콜백은 <b>첫 구독이 남기고 간 죽은 StateFlow</b> 를 계속 가리켜서
     * 매칭 메시지는 도착하는데 화면에는 영영 오지 않는다.
     *
     * replay 는 0 이다. 매칭 성공은 상태가 아니라 일회성 이벤트라, 지난 배틀의 matchId 가
     * 새 구독자에게 재생되면 이전 배틀 화면으로 들어가 버린다.
     */
    private val matchedEventMap = ConcurrentHashMap<String, MutableSharedFlow<MatchedEvent>>()

    /**
     * 매치 큐 Flow 조회
     */
    override suspend fun createMatchQueue(mongId: Long, deviceId: String): Flow<MatchQueue?> =
        channelFlow {

            val matchedEvent = matchedEventMap.computeIfAbsent(deviceId) {
                MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
            }
            val subscribeCount = subscribeCounterMap.computeIfAbsent(deviceId) { AtomicInteger(0) }
            val topic = "${context.getString(R.string.mongs_mqtt_topic)}/battle/queue/$deviceId"

            // 아직 매칭 전이라는 뜻. BaseViewModel.observeForever 가 첫 값까지 호출자를
            // 붙잡아 두므로 구독보다 먼저 흘린다 (아래 주석 참고).
            send(null)

            if (subscribeCount.getAndIncrement() == 0) {
                // 구독은 따로 띄운다. subscribe 는 브로커 SUBACK 까지 suspend 하는데,
                // 그걸 기다리는 동안 호출자가 멈추면 대기열 등록이 그만큼 밀린다.
                launch {
                    mqttClient.subscribe(
                        topic = topic,
                        classType = MatchQueueEventDto::class.java,
                        onReceive = { responseDto ->
                            matchedEvent.tryEmit(
                                MatchedEvent(
                                    matchId = responseDto.result.matchId,
                                    playerId = responseDto.result.matchPlayers
                                        .find { it.deviceId == deviceId }?.playerId ?: "",
                                )
                            )
                        }
                    )
                }
            }

            launch {
                matchedEvent.collect {
                    send(
                        MatchQueue(
                            deviceId = deviceId,
                            mongId = mongId,
                            matchId = it.matchId,
                            playerId = it.playerId,
                        )
                    )
                }
            }

            awaitClose {
                if (subscribeCount.decrementAndGet() == 0) {
                    subscribeCounterMap.remove(deviceId)
                    cleanUpScope.launch { mqttClient.disSubscribe(topic = topic) }
                }
            }
        }

    private companion object {
        /**
         * awaitClose 는 취소 경로에서 돌아 suspend 를 부를 수 없다.
         * 구독 해제는 별도 스코프에 맡긴다 (disSubscribe 자체가 NonCancellable 이다).
         */
        val cleanUpScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

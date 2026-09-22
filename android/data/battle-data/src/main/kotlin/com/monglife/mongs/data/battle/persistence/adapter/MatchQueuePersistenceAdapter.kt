package com.monglife.mongs.data.battle.persistence.adapter

import android.content.Context
import com.monglife.core.data.flow.SharedFlowCache
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.battle.port.persistence.MatchQueuePersistencePort
import com.monglife.mongs.data.battle.persistence.dto.MatchQueueEventDto
import com.monglife.mongs.domain.battle.model.MatchQueue
import com.mongs.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class MatchQueuePersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttClient: MqttClient,
) : MatchQueuePersistencePort {

    private val subscribeCounterMap = ConcurrentHashMap<Long, AtomicInteger>()

    /**
     * (mongId, deviceId) 별 SharedFlow 캐시
     * 호출할 때마다 shareIn 을 새로 하면 공유가 되지 않아 구독자마다 MQTT 구독이 중복된다.
     */
    private val matchQueueFlowCache = SharedFlowCache<Pair<Long, String>, MatchQueue?>()

    /**
     * 매치 큐 Flow 조회
     */
    override suspend fun createMatchQueue(mongId: Long, deviceId: String): Flow<MatchQueue?> =
        matchQueueFlowCache.getOrCreate(key = mongId to deviceId) {
            createMatchQueueFlow(mongId = mongId, deviceId = deviceId)
        }

    private fun createMatchQueueFlow(mongId: Long, deviceId: String): Flow<MatchQueue?> =
        flow {

            val matchQueue = MutableStateFlow<MatchQueue?>(null)
            val subscribeCount = subscribeCounterMap.getOrPut(mongId) { AtomicInteger(0) }
            val topic = "${context.getString(R.string.mongs_mqtt_topic)}/battle/queue/$deviceId"

            if (subscribeCount.getAndIncrement() == 0) {
                mqttClient.subscribe(
                    topic = topic,
                    classType = MatchQueueEventDto::class.java,
                    onReceive = { responseDto ->
                        matchQueue.value = MatchQueue(
                            deviceId = deviceId,
                            mongId = mongId,
                            matchId = responseDto.result.matchId,
                            playerId = responseDto.result.matchPlayers.find { it.deviceId == deviceId }?.playerId ?: "",
                        )
                    }
                )
            }

            try {
                emitAll(matchQueue)
            } finally {
                if (subscribeCount.decrementAndGet() == 0) {
                    mqttClient.disSubscribe(topic = topic)
                    subscribeCounterMap.remove(mongId)
                }
            }
        }
}
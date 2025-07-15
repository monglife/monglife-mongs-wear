package com.monglife.mongs.data.battle.persistence.adapter

import android.content.Context
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.battle.port.persistence.MatchQueuePersistencePort
import com.monglife.mongs.data.battle.persistence.dto.MatchQueueEventDto
import com.monglife.mongs.domain.battle.model.MatchQueue
import com.mongs.wear.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class MatchQueuePersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttClient: MqttClient,
) : MatchQueuePersistencePort {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val subscribeCounterMap = ConcurrentHashMap<Long, AtomicInteger>()

    /**
     * 매치 큐 Flow 조회
     */
    override suspend fun createMatchQueue(mongId: Long, deviceId: String): Flow<MatchQueue?> =
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
                            playerId = responseDto.result.matchPlayers.find { it.deviceId == deviceId }?.playerId
                                ?: "",
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
        }.shareIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1,
        )
}
package com.monglife.mongs.data.battle.persistence.adapter

import android.content.Context
import com.monglife.core.data.flow.SharedFlowCache
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.data.battle.persistence.dto.MatchEventDto
import com.monglife.mongs.domain.battle.enums.MatchStateCode
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import com.mongs.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class MatchPersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttClient: MqttClient,
) : MatchPersistencePort {

    private val subscribeCounterMap = ConcurrentHashMap<Long, AtomicInteger>()

    /**
     * matchId 별 SharedFlow 캐시
     * 호출할 때마다 shareIn 을 새로 하면 공유가 되지 않아 구독자마다 MQTT 구독이 중복된다.
     */
    private val matchFlowCache = SharedFlowCache<Long, Match?>()

    /**
     * 매치 Flow 조회
     */
    override suspend fun getMatchFlow(matchId: Long): Flow<Match?> =
        matchFlowCache.getOrCreate(key = matchId) { createMatchFlow(matchId = matchId) }

    private fun createMatchFlow(matchId: Long): Flow<Match?> = flow {

        val match = MutableStateFlow<Match?>(null)
        val subscribeCount = subscribeCounterMap.getOrPut(matchId) { AtomicInteger(0) }
        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/battle/match/$matchId"

        if (subscribeCount.getAndIncrement() == 0) {
            mqttClient.subscribe(
                topic = topic,
                classType = MatchEventDto::class.java,
                onReceive = { responseDto ->
                    match.value = Match(
                        matchId = responseDto.result.matchId,
                        round = responseDto.result.round,
                        isLastRound = responseDto.result.isLastRound,
                        stateCode = MatchStateCode.MATCH,
                        matchPlayers = responseDto.result.matchPlayers.map {
                            MatchPlayer(
                                playerId = it.playerId,
                                deviceId = it.deviceId,
                                mongCode = it.mongCode,
                                mongName = it.mongName,
                                name = it.name,
                                hp = it.hp,
                                roundCode = it.roundCode,
                            )
                        }
                    )
                }
            )
        }

        try {
            emitAll(match)
        } finally {
            if (subscribeCount.decrementAndGet() == 0) {
                mqttClient.disSubscribe(topic = topic)
                subscribeCounterMap.remove(matchId)
            }
        }
    }
}
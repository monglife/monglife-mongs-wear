package com.monglife.mongs.data.battle.persistence.adapter

import android.content.Context
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.data.battle.persistence.dto.MatchEventDto
import com.monglife.mongs.domain.battle.enums.MatchStateCode
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import com.mongs.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class MatchPersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttClient: MqttClient,
) : MatchPersistencePort {

    private val subscribeCounterMap = ConcurrentHashMap<Long, AtomicInteger>()

    /**
     * matchId 별 매치 상태.
     *
     * <b>MQTT 콜백이 쓰는 대상은 어댑터가 소유해야 한다.</b> flow 본문에서 만든 지역
     * StateFlow 에 콜백을 묶으면, 중복 구독 가드로 subscribe 를 건너뛴 두 번째 구독이
     * 죽은 StateFlow 를 가리켜 메시지가 화면까지 오지 않는다.
     *
     * 키가 matchId 라 배틀마다 새로 생긴다 - 지난 매치 상태가 재생될 일은 없다.
     */
    private val matchStateMap = ConcurrentHashMap<Long, MutableStateFlow<Match?>>()

    /**
     * 매치 Flow 조회
     */
    override suspend fun getMatchFlow(matchId: Long): Flow<Match?> =
        createMatchFlow(matchId = matchId)

    private fun createMatchFlow(matchId: Long): Flow<Match?> = channelFlow {

        val match = matchStateMap.computeIfAbsent(matchId) { MutableStateFlow(null) }
        val subscribeCount = subscribeCounterMap.getOrPut(matchId) { AtomicInteger(0) }
        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/battle/match/$matchId"

        // 매치 상태를 먼저 흘려보낸다. BaseViewModel.observeForever 는 첫 값까지 호출자를
        // 붙잡아 두는데, 그 뒤에 매치 입장 발행이 서 있다. 구독 SUBACK 을 기다리게 두면
        // 입장이 그만큼 밀리고, 30초를 넘기면 서버가 매치를 통째로 CANCELED 로 마감한다.
        // (실기기에서 2회차 배틀이 이 경로로 죽었다 - 메시지는 오는데 SUBACK 이 늦었다.)
        launch {
            match.collect { send(it) }
        }

        if (subscribeCount.getAndIncrement() == 0) {
            // 구독은 따로 띄운다. 느려도 위의 수신 경로와 입장 발행을 막지 않는다.
            launch {
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
        }

        awaitClose {
            if (subscribeCount.decrementAndGet() == 0) {
                subscribeCounterMap.remove(matchId)
                matchStateMap.remove(matchId)
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

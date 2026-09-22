package com.monglife.mongs.data.battle.publish.adapter

import android.content.Context
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchEnterException
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchExitException
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchPickException
import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import com.monglife.mongs.data.battle.publish.dto.MatchEnterEventDto
import com.monglife.mongs.data.battle.publish.dto.MatchExitEventDto
import com.monglife.mongs.data.battle.publish.dto.MatchPickEventDto
import com.mongs.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MatchPublishAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mqttClient: MqttClient,
) : MatchPublishPort {

    /**
     * 매치 입장
     */
    @Throws(InvalidPublishMatchEnterException::class)
    override suspend fun publishMatchEnter(matchId: Long, playerId: String) {

        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/battle/match/enter/$matchId"

        mqttClient.publish(
            topic = topic,
            requestDto = MatchEnterEventDto(playerId = playerId)
        )
    }

    /**
     * 매치 선택
     */
    @Throws(InvalidPublishMatchPickException::class)
    override suspend fun publishMatchPick(
        matchId: Long,
        playerId: String,
        targetPlayerId: String,
        pickCode: String
    ) {

        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/battle/match/pick/$matchId"

        mqttClient.publish(
            topic = topic,
            requestDto = MatchPickEventDto(
                playerId = playerId,
                targetPlayerId = targetPlayerId,
                pickCode = pickCode
            )
        )
    }

    /**
     * 매치 퇴장
     */
    @Throws(InvalidPublishMatchExitException::class)
    override suspend fun publishMatchExit(matchId: Long, playerId: String) {

        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/battle/match/exit/$matchId"

        mqttClient.publish(
            topic = topic,
            requestDto = MatchExitEventDto(playerId = playerId)
        )
    }
}
package com.monglife.mongs.data.member.player.persistence.adapter

import android.content.Context
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.core.data.persistence.datastore.SessionDataStore
import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.data.member.player.persistence.datastore.PlayerDataStore
import com.monglife.mongs.data.member.player.persistence.dto.PlayerSlotCountEventDto
import com.monglife.mongs.data.member.player.persistence.dto.PlayerStarPointEventDto
import com.monglife.mongs.data.member.player.persistence.entity.PlayerEntity
import com.monglife.mongs.domain.member.player.model.Player
import com.mongs.wear.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerPersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionDataStore: SessionDataStore,
    private val playerDataStore: PlayerDataStore,
    private val mqttClient: MqttClient,
) : PlayerPersistencePort {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val subscribeCounterMap = ConcurrentHashMap<Long, AtomicInteger>()

    /**
     * 플레이어 조회
     */
    override suspend fun getPlayer(): Player? = playerDataStore.getPlayer()?.toDomain()
        ?: sessionDataStore.getSession()?.let {
            playerDataStore.savePlayer(
                playerEntity = PlayerEntity(
                    accountId = it.accountId,
                    slotCount = 0,
                    starPoint = 0,
                )
            ).toDomain()
        }

    /**
     * 플레이어 Flow 객체 조회
     */
    override suspend fun getPlayerFlow(): Flow<Player?> = flow {
        val playerEntity = playerDataStore.getPlayer() ?: sessionDataStore.getSession()?.let {
            playerDataStore.savePlayer(
                playerEntity = PlayerEntity(
                    accountId = it.accountId,
                    slotCount = 0,
                    starPoint = 0,
                )
            )
        }

        playerEntity?.let {
            val subscribeCount =
                subscribeCounterMap.getOrPut(it.accountId) { AtomicInteger(0) }
            val starPointSubscribeTopic =
                "${context.getString(R.string.mongs_mqtt_topic)}/member/${it.accountId}/starPoint"
            val slotCountSubscribeTopic =
                "${context.getString(R.string.mongs_mqtt_topic)}/member/${it.accountId}/slotCount"

            if (subscribeCount.getAndIncrement() == 0) {
                mqttClient.subscribe(
                    topic = starPointSubscribeTopic,
                    classType = PlayerStarPointEventDto::class.java,
                    onReceive = { responseDto: ResponseDto<PlayerStarPointEventDto> ->
                        playerDataStore.savePlayer(
                            playerEntity = PlayerEntity(
                                accountId = responseDto.result.accountId,
                                slotCount = it.slotCount,
                                starPoint = responseDto.result.starPoint,
                            )
                        )
                    }
                )
                mqttClient.subscribe(
                    topic = slotCountSubscribeTopic,
                    classType = PlayerSlotCountEventDto::class.java,
                    onReceive = { responseDto: ResponseDto<PlayerSlotCountEventDto> ->
                        playerDataStore.savePlayer(
                            playerEntity = PlayerEntity(
                                accountId = responseDto.result.accountId,
                                slotCount = responseDto.result.slotCount,
                                starPoint = it.starPoint,
                            )
                        )
                    }
                )
            }

            try {
                emitAll(playerDataStore.getPlayerFlow().map { it?.toDomain() })
            } finally {
                if (subscribeCount.decrementAndGet() == 0) {
                    mqttClient.disSubscribe(topic = starPointSubscribeTopic)
                    mqttClient.disSubscribe(topic = slotCountSubscribeTopic)
                    subscribeCounterMap.remove(it.accountId)
                }
            }
        }
    }.shareIn(
        scope = applicationScope,
        started = SharingStarted.WhileSubscribed(),
        replay = 1,
    )

    /**
     * 플레이어 로컬 동기화
     */
    override suspend fun savePlayer(player: Player): Player =
        playerDataStore.savePlayer(
            playerEntity = PlayerEntity(
                accountId = player.accountId,
                slotCount = player.slotCount,
                starPoint = player.starPoint,
            )
        ).toDomain()
}
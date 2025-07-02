package com.monglife.mongs.data.member.player.persistence.adapter

import android.content.Context
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.core.data.mqtt.utils.MqttUtil
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerPersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerDataStore: PlayerDataStore,
    private val mqttClient: MqttClient,
    private val mqttUtil: MqttUtil,
) : PlayerPersistencePort {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val subscribeCounterMap = ConcurrentHashMap<Long, AtomicInteger>()

    /**
     * 플레이어 조회
     */
    @Throws(NotFoundPlayerException::class)
    override suspend fun getPlayer(): Player =
        playerDataStore.getPlayer()?.toDomain() ?: throw NotFoundPlayerException()

    /**
     * 플레이어 Flow 객체 조회
     */
    @Throws(NotFoundPlayerException::class)
    override suspend fun getPlayerFlow(): Flow<Player> = flow {

        val player = playerDataStore.getPlayer()?.toDomain() ?: throw NotFoundPlayerException()

        val subscribeCount = subscribeCounterMap.getOrPut(player.accountId) { AtomicInteger(0) }

        val baseTopic = "${context.getString(R.string.mongs_mqtt_topic)}/member"
        val topic = "$baseTopic/${player.accountId}/#"
        val starPointTopic = "$baseTopic/${player.accountId}/starPoint"
        val slotCountTopic = "$baseTopic/${player.accountId}/slotCount"

        if (subscribeCount.getAndIncrement() == 0) {
            mqttClient.subscribe(topic = topic, callback = object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {}
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (message == null || topic == null) return
                    applicationScope.launch {
                        Mutex().withLock(owner = applicationScope) {
                            when (topic) {
                                starPointTopic -> mqttUtil.fromJson(
                                    mqttMessage = message,
                                    classType = PlayerStarPointEventDto::class.java
                                ).let { responseDto ->
                                    playerDataStore.getPlayer()?.let {
                                        playerDataStore.savePlayer(
                                            playerEntity = PlayerEntity(
                                                accountId = responseDto.result.accountId,
                                                slotCount = it.slotCount,
                                                starPoint = responseDto.result.starPoint,
                                            )
                                        )
                                    }
                                }

                                slotCountTopic -> mqttUtil.fromJson(
                                    mqttMessage = message,
                                    classType = PlayerSlotCountEventDto::class.java
                                ).let { responseDto ->
                                    playerDataStore.getPlayer()?.let {
                                        playerDataStore.savePlayer(
                                            playerEntity = PlayerEntity(
                                                accountId = responseDto.result.accountId,
                                                slotCount = responseDto.result.slotCount,
                                                starPoint = it.starPoint,
                                            )
                                        )
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }
            })
        }

        try {
            emitAll(
                playerDataStore.getPlayerFlow()
                    .map { it?.toDomain() ?: throw NotFoundPlayerException() })
        } finally {
            if (subscribeCount.decrementAndGet() == 0) {
                mqttClient.disSubscribe(topic = topic)
                subscribeCounterMap.remove(player.accountId)
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
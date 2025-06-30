package com.monglife.mongs.data.member.player.persistence.adapter

import android.content.Context
import android.util.Log
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.data.core.mqtt.client.MqttClient
import com.monglife.mongs.data.member.player.persistence.datastore.PlayerDataStore
import com.monglife.mongs.data.member.player.persistence.entity.PlayerEntity
import com.monglife.mongs.domain.member.player.model.Player
import com.mongs.wear.data.core.R
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerPersistenceAdapterModule {
    @Binds
    @Singleton
    abstract fun bindPlayerPersistencePort(adapter: PlayerPersistenceAdapter): PlayerPersistencePort
}

@Singleton
class PlayerPersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerDataStore: PlayerDataStore,
    private val mqttClient: MqttClient,
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

        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/member/${player.accountId}/#"
        val subscribeCount = subscribeCounterMap.getOrPut(player.accountId) { AtomicInteger(0) }

        if (subscribeCount.getAndIncrement() == 0) {
            mqttClient.subscribe(topic = topic, callback = object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {}
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    // TODO: MQTT 수신 업데이트 구현
                    Log.d("TEST", "${message}")
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
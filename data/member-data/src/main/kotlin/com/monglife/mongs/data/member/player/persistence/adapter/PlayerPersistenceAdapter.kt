package com.monglife.mongs.data.member.player.persistence.adapter

import android.util.Log
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.data.member.player.persistence.datastore.PlayerDataStore
import com.monglife.mongs.data.member.player.persistence.entity.PlayerEntity
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerPersistenceAdapter @Inject constructor(
    private val playerDataStore: PlayerDataStore,
) : PlayerPersistencePort {

    private val subscribeCount = AtomicInteger(0)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        if (subscribeCount.getAndIncrement() == 0) {
            // TODO: MQTT 구독
            Log.d("TEST", "subscribe player")
        }

        try {
            emitAll(playerDataStore.getPlayerFlow().map {
                it?.toDomain() ?: throw NotFoundPlayerException()
            })
        } finally {
            // TODO: MQTT 구독 해제
            if (subscribeCount.decrementAndGet() == 0) {
                Log.d("TEST", "disSubscribe player")
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
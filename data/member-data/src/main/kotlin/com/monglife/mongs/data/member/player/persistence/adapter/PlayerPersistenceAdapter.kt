package com.monglife.mongs.data.member.player.persistence.adapter

import androidx.datastore.preferences.core.edit
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.data.member.player.persistence.datastore.PlayerDataStore
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerPersistenceAdapter @Inject constructor(
    private val playerDataStore: PlayerDataStore,
) : PlayerPersistencePort {

    /**
     * 플레이어 조회
     */
    @Throws(NotFoundPlayerException::class)
    override suspend fun getPlayer(): Player =
        playerDataStore.getStore().data.map {
            if (!it.contains(PlayerDataStore.ACCOUNT_ID)) throw NotFoundPlayerException()
            Player(
                accountId = it[PlayerDataStore.ACCOUNT_ID]!!,
                slotCount = it[PlayerDataStore.SLOT_COUNT] ?: 1,
                starPoint = it[PlayerDataStore.STAR_POINT] ?: 0,
            )
        }.first()

    /**
     * 플레이어 라이브 객체 조회
     */
    @Throws(NotFoundPlayerException::class)
    override suspend fun getPlayerFlow(): Flow<Player> =
        playerDataStore.getStore().data.map {
            if (!it.contains(PlayerDataStore.ACCOUNT_ID)) throw NotFoundPlayerException()
            Player(
                accountId = it[PlayerDataStore.ACCOUNT_ID]!!,
                slotCount = it[PlayerDataStore.SLOT_COUNT] ?: 1,
                starPoint = it[PlayerDataStore.STAR_POINT] ?: 0,
            )
        }

    /**
     * 플레이어 로컬 동기화
     */
    override suspend fun savePlayer(player: Player): Player {
        playerDataStore.getStore().edit { preferences ->
            preferences[PlayerDataStore.ACCOUNT_ID] = player.accountId
            preferences[PlayerDataStore.SLOT_COUNT] = player.slotCount
            preferences[PlayerDataStore.STAR_POINT] = player.starPoint
        }

        return playerDataStore.getStore().data.map {
            Player(
                accountId = it[PlayerDataStore.ACCOUNT_ID]!!,
                slotCount = it[PlayerDataStore.SLOT_COUNT]!!,
                starPoint = it[PlayerDataStore.STAR_POINT]!!,
            )
        }.first()
    }
}
package com.monglife.mongs.data.member.player.persistence.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monglife.mongs.data.member.player.persistence.entity.PlayerEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "PLAYER")

    companion object {
        private val ACCOUNT_ID = longPreferencesKey("accountId")
        private val SLOT_COUNT = intPreferencesKey("slotCount")
        private val STAR_POINT = intPreferencesKey("starPoint")
    }

    /**
     * 플레이어 조회
     */
    suspend fun getPlayer(): PlayerEntity? = context.store.data.map {
        if (it.contains(ACCOUNT_ID) && it.contains(SLOT_COUNT) && it.contains(STAR_POINT)) {
            PlayerEntity(
                accountId = it[ACCOUNT_ID]!!,
                slotCount = it[SLOT_COUNT]!!,
                starPoint = it[STAR_POINT]!!
            )
        } else {
            null
        }
    }.first()

    /**
     * 플레이어 조회
     */
    fun getPlayerFlow(): Flow<PlayerEntity?> = context.store.data.map {
        if (it.contains(ACCOUNT_ID) && it.contains(SLOT_COUNT) && it.contains(STAR_POINT)) {
            PlayerEntity(
                accountId = it[ACCOUNT_ID]!!,
                slotCount = it[SLOT_COUNT]!!,
                starPoint = it[STAR_POINT]!!
            )
        } else {
            null
        }
    }

    /**
     * 플레이어 저장
     */
    suspend fun savePlayer(playerEntity: PlayerEntity): PlayerEntity = context.store.let { store ->
        store.edit { preferences ->
            preferences[ACCOUNT_ID] = playerEntity.accountId
            preferences[SLOT_COUNT] = playerEntity.slotCount
            preferences[STAR_POINT] = playerEntity.starPoint
        }

        store.data.map {
            PlayerEntity(
                accountId = it[ACCOUNT_ID]!!,
                slotCount = it[SLOT_COUNT]!!,
                starPoint = it[STAR_POINT]!!
            )
        }.first()
    }

    /**
     * 플레이어 삭제
     */
    suspend fun deletePlayer(): PlayerEntity = context.store.let { store ->
        val playerEntity = store.data.map {
            PlayerEntity(
                accountId = it[ACCOUNT_ID]!!,
                slotCount = it[SLOT_COUNT]!!,
                starPoint = it[STAR_POINT]!!
            )
        }

        store.edit { preferences ->
            preferences.remove(ACCOUNT_ID)
            preferences.remove(SLOT_COUNT)
            preferences.remove(STAR_POINT)
        }

        playerEntity.first()
    }
}
package com.monglife.mongs.data.core.persistence.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monglife.mongs.data.core.persistence.entity.SessionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "SESSION")

    companion object {
        private val ACCOUNT_ID = longPreferencesKey("accountId")
        private val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        private val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
        private val VERSION = longPreferencesKey("version")
    }

    /**
     * 세션 조회
     */
    suspend fun getSession(): SessionEntity? = context.store.data.map {
        if (
            it.contains(ACCOUNT_ID) &&
            it.contains(ACCESS_TOKEN) &&
            it.contains(REFRESH_TOKEN) &&
            it.contains(VERSION)
        ) {
            SessionEntity(
                accountId = it[ACCOUNT_ID]!!,
                accessToken = it[ACCESS_TOKEN]!!,
                refreshToken = it[REFRESH_TOKEN]!!,
                version = it[VERSION]!!
            )
        } else {
            null
        }
    }.first()

    /**
     * 세션 조회
     */
    fun getSessionFlow(): Flow<SessionEntity?> = context.store.data.map {
        if (
            it.contains(ACCOUNT_ID) &&
            it.contains(ACCESS_TOKEN) &&
            it.contains(REFRESH_TOKEN) &&
            it.contains(VERSION)
        ) {
            SessionEntity(
                accountId = it[ACCOUNT_ID]!!,
                accessToken = it[ACCESS_TOKEN]!!,
                refreshToken = it[REFRESH_TOKEN]!!,
                version = it[VERSION]!!
            )
        } else {
            null
        }
    }

    /**
     * 세션 저장
     */
    suspend fun saveSession(sessionEntity: SessionEntity): SessionEntity = context.store.let { store ->
        store.edit { preferences ->
            preferences[ACCOUNT_ID] = sessionEntity.accountId
            preferences[ACCESS_TOKEN] = sessionEntity.accessToken
            preferences[REFRESH_TOKEN] = sessionEntity.refreshToken
            preferences[VERSION] = sessionEntity.version
        }

        store.data.map {
            SessionEntity(
                accountId = it[ACCOUNT_ID]!!,
                accessToken = it[ACCESS_TOKEN]!!,
                refreshToken = it[REFRESH_TOKEN]!!,
                version = it[VERSION]!!
            )
        }.first()
    }

    /**
     * 세션 삭제
     */
    suspend fun deleteSession() = context.store.let { store ->
        store.edit { preferences ->
            preferences.remove(ACCOUNT_ID)
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(VERSION)
        }
    }
}
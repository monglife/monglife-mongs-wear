package com.monglife.mongs.data.auth.persistence.adapter

import androidx.datastore.preferences.core.edit
import com.monglife.mongs.data.core.retrofit.datastore.SessionDataStore
import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPersistenceAdapter @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) : com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort,
    com.monglife.mongs.application.device.port.persistence.AuthPersistencePort,
    com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort,
    com.monglife.mongs.application.mong.port.persistence.AuthPersistencePort {

    /**
     * 세션 존재 여부 조회
     */
    override suspend fun isExistsSession(): Boolean = sessionDataStore.getStore().data.map {
        it[SessionDataStore.ACCOUNT_ID] != SessionDataStore.ACCOUNT_INIT_VALUE
    }.first()

    /**
     * 세션 존재 라이브 객체 조회
     */
    override suspend fun isExistsSessionFlow(): Flow<Boolean> =
        sessionDataStore.getStore().data.map {
            it[SessionDataStore.ACCOUNT_ID] != SessionDataStore.ACCOUNT_INIT_VALUE
        }

    /**
     * 세션 조회
     */
    override suspend fun getSession(): Session? = sessionDataStore.getStore().data.map {

        val accountId = it[SessionDataStore.ACCOUNT_ID]
        val accessToken = it[SessionDataStore.ACCESS_TOKEN]
        val refreshToken = it[SessionDataStore.REFRESH_TOKEN]

        if (accountId != SessionDataStore.ACCOUNT_INIT_VALUE && accessToken != SessionDataStore.ACCESS_TOKEN_INIT_VALUE && refreshToken != SessionDataStore.REFRESH_TOKEN_INIT_VALUE) {
            Session(
                accountId = it[SessionDataStore.ACCOUNT_ID]!!,
                accessToken = it[SessionDataStore.ACCESS_TOKEN]!!,
                refreshToken = it[SessionDataStore.REFRESH_TOKEN]!!,
            )
        } else {
            null
        }
    }.first()

    /**
     * 세션 저장
     */
    override suspend fun saveSession(session: Session): Session =
        sessionDataStore.getStore().let { store ->

            store.edit { preferences ->
                preferences[SessionDataStore.ACCOUNT_ID] = session.accountId
                preferences[SessionDataStore.ACCESS_TOKEN] = session.accessToken
                preferences[SessionDataStore.REFRESH_TOKEN] = session.refreshToken
            }

            store.data.map {
                Session(
                    accountId = it[SessionDataStore.ACCOUNT_ID]!!,
                    accessToken = it[SessionDataStore.ACCESS_TOKEN]!!,
                    refreshToken = it[SessionDataStore.REFRESH_TOKEN]!!,
                )
            }.first()
        }

    /**
     * 세션 삭제
     */
    override suspend fun deleteSession(): Session = sessionDataStore.getStore().let { store ->

        val session = store.data.map {
            Session(
                accountId = it[SessionDataStore.ACCOUNT_ID]!!,
                accessToken = it[SessionDataStore.ACCESS_TOKEN]!!,
                refreshToken = it[SessionDataStore.REFRESH_TOKEN]!!,
            )
        }

        store.edit { preferences ->
            preferences[SessionDataStore.ACCOUNT_ID] = SessionDataStore.ACCOUNT_INIT_VALUE
            preferences[SessionDataStore.ACCESS_TOKEN] = SessionDataStore.ACCESS_TOKEN_INIT_VALUE
            preferences[SessionDataStore.REFRESH_TOKEN] = SessionDataStore.REFRESH_TOKEN_INIT_VALUE
        }

        return session.first()
    }
}
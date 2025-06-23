package com.monglife.mongs.data.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monglife.mongs.domain.auth.model.Session
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "SESSION")

    companion object {
        val ACCOUNT_ID = longPreferencesKey("accountId")
        val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
        val VERSION = longPreferencesKey("version")

        const val ACCOUNT_INIT_VALUE = Long.MIN_VALUE
        const val ACCESS_TOKEN_INIT_VALUE = ""
        const val REFRESH_TOKEN_INIT_VALUE = ""
        const val VERSION_INIT_VALUE = 0L
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.store.edit { preferences ->
                if (!preferences.contains(ACCOUNT_ID)) preferences[ACCOUNT_ID] = ACCOUNT_INIT_VALUE
                if (!preferences.contains(ACCESS_TOKEN)) preferences[ACCESS_TOKEN] = ACCESS_TOKEN_INIT_VALUE
                if (!preferences.contains(REFRESH_TOKEN)) preferences[REFRESH_TOKEN] = REFRESH_TOKEN_INIT_VALUE
                if (!preferences.contains(VERSION)) preferences[VERSION] = VERSION_INIT_VALUE
            }
        }
    }

    /**
     * 세션 조회 (뮤텍스)
     */
    suspend fun getSession(): Session? = context.store.data.map {
        val accountId = it[ACCOUNT_ID]
        val accessToken = it[ACCESS_TOKEN]
        val refreshToken = it[REFRESH_TOKEN]
        val version = it[VERSION]

        if (accountId != ACCOUNT_INIT_VALUE && accessToken != ACCESS_TOKEN_INIT_VALUE && refreshToken != REFRESH_TOKEN_INIT_VALUE && version != VERSION_INIT_VALUE) {
            Session(
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
    fun getSessionFlow(): Flow<Session?> = context.store.data.map {
        val accountId = it[ACCOUNT_ID]
        val accessToken = it[ACCESS_TOKEN]
        val refreshToken = it[REFRESH_TOKEN]
        val version = it[VERSION]

        if (accountId != ACCOUNT_INIT_VALUE && accessToken != ACCESS_TOKEN_INIT_VALUE && refreshToken != REFRESH_TOKEN_INIT_VALUE && version != VERSION_INIT_VALUE) {
            Session(
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
    suspend fun saveSession(session: Session): Session = context.store.let { store ->
        store.edit { preferences ->
            preferences[ACCOUNT_ID] = session.accountId
            preferences[ACCESS_TOKEN] = session.accessToken
            preferences[REFRESH_TOKEN] = session.refreshToken
            preferences[VERSION] = session.version
        }

        store.data.map {
            Session(
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
    suspend fun deleteSession(): Session = context.store.let { store ->
        val session = store.data.map {
            Session(
                accountId = it[ACCOUNT_ID]!!,
                accessToken = it[ACCESS_TOKEN]!!,
                refreshToken = it[REFRESH_TOKEN]!!,
                version = it[VERSION]!!
            )
        }

        store.edit { preferences ->
            preferences[ACCOUNT_ID] = ACCOUNT_INIT_VALUE
            preferences[ACCESS_TOKEN] = ACCESS_TOKEN_INIT_VALUE
            preferences[REFRESH_TOKEN] = REFRESH_TOKEN_INIT_VALUE
            preferences[VERSION] = VERSION_INIT_VALUE
        }

        session.first()
    }
}
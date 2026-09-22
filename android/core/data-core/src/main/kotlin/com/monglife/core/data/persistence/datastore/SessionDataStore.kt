package com.monglife.core.data.persistence.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monglife.core.data.persistence.crypto.SessionCipher
import com.monglife.core.data.persistence.entity.SessionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cipher: SessionCipher,
) {
    private val Context.store by preferencesDataStore(name = "SESSION")

    companion object {
        private val ACCOUNT_ID = longPreferencesKey("accountId")
        private val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        private val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
        private val VERSION = longPreferencesKey("version")
    }

    /**
     * 복호화 결과 캐시
     *
     * AuthorizationInterceptor 가 모든 HTTP 요청마다 세션을 읽는데,
     * Keystore 의 Cipher.init 은 TEE 왕복이라 요청마다 붙으면 워치에서 체감된다.
     * 저장된 암호문 자체를 캐시 키로 써서 값이 바뀌면 자동으로 캐시가 빗나가게 한다.
     * (별도 무효화 로직을 두지 않으므로 갱신 누락 버그가 생기지 않는다)
     */
    private class Decoded(
        val rawAccessToken: String,
        val rawRefreshToken: String,
        val entity: SessionEntity,
    )

    @Volatile
    private var cache: Decoded? = null

    private val migrationMutex = Mutex()

    @Volatile
    private var migrationDone = false

    /**
     * 저장된 값을 평문으로 되돌린다.
     * 접두사가 없으면 암호화 이전 버전이 남긴 평문으로 본다.
     */
    private fun decode(stored: String): String? =
        if (stored.startsWith(SessionCipher.MARKER)) cipher.decrypt(stored) else stored

    private fun toEntity(preferences: Preferences): SessionEntity? {
        val rawAccessToken = preferences[ACCESS_TOKEN] ?: return null
        val rawRefreshToken = preferences[REFRESH_TOKEN] ?: return null
        val accountId = preferences[ACCOUNT_ID] ?: return null
        val version = preferences[VERSION] ?: return null

        cache
            ?.takeIf { it.rawAccessToken == rawAccessToken && it.rawRefreshToken == rawRefreshToken }
            ?.let { return it.entity }

        // 복호화 실패 = 키 소실. 세션 없음으로 떨어뜨려 로그인 화면으로 보낸다.
        val accessToken = decode(rawAccessToken) ?: return null
        val refreshToken = decode(rawRefreshToken) ?: return null

        return SessionEntity(
            accountId = accountId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            version = version,
        ).also {
            cache = Decoded(
                rawAccessToken = rawAccessToken,
                rawRefreshToken = rawRefreshToken,
                entity = it,
            )
        }
    }

    private fun isLegacyPlainText(preferences: Preferences): Boolean =
        preferences[ACCESS_TOKEN]?.startsWith(SessionCipher.MARKER) == false

    /**
     * 세션 조회
     */
    suspend fun getSession(): SessionEntity? {
        val preferences = context.store.data.first()
        val session = toEntity(preferences)

        if (session == null) {
            // 값은 있는데 복호화가 안 되는 상태. 매 요청마다 Keystore 를 두드리지 않도록 정리한다.
            if (preferences[ACCESS_TOKEN] != null) deleteSession()
            return null
        }

        if (isLegacyPlainText(preferences)) migrateIfNeeded(session)

        return session
    }

    /**
     * 세션 조회
     */
    fun getSessionFlow(): Flow<SessionEntity?> = context.store.data
        // 이 경로에서는 쓰기를 하지 않는다. store.edit 은 data 재발행을 유발해 재진입이 된다.
        .map { toEntity(it) }
        .distinctUntilChanged()

    /**
     * 세션 저장
     */
    suspend fun saveSession(sessionEntity: SessionEntity): SessionEntity {
        context.store.edit { preferences ->
            preferences[ACCOUNT_ID] = sessionEntity.accountId
            preferences[ACCESS_TOKEN] = cipher.encrypt(sessionEntity.accessToken)
            preferences[REFRESH_TOKEN] = cipher.encrypt(sessionEntity.refreshToken)
            preferences[VERSION] = sessionEntity.version
        }

        /**
         * 방금 쓴 값을 다시 읽어 오지 않는다.
         * 읽기 왕복에 복호화까지 붙고, 그 사이 deleteSession 이 끼면 NPE 가 났다.
         */
        return sessionEntity
    }

    /**
     * 세션 삭제
     *
     * Keystore alias 는 지우지 않는다. 동시에 saveSession 이 진행 중이면
     * 방금 암호화한 값이 사라진 키를 참조하게 된다. 암호문이 없으면 키만 남아도 무해하다.
     */
    suspend fun deleteSession() {
        cache = null

        context.store.edit { preferences ->
            preferences.remove(ACCOUNT_ID)
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(VERSION)
        }
    }

    /**
     * 암호화 이전 버전이 남긴 평문 토큰을 암호문으로 옮긴다.
     *
     * 값 자체는 그대로라 getSessionFlow 의 distinctUntilChanged 를 통과하지 못하고,
     * 구독자에게 재발행되지 않아 화면 깜빡임이 없다. 재로그인도 강제하지 않는다.
     */
    private suspend fun migrateIfNeeded(sessionEntity: SessionEntity) {
        if (migrationDone) return

        migrationMutex.withLock {
            if (migrationDone) return

            saveSession(sessionEntity)
            migrationDone = true
        }
    }
}

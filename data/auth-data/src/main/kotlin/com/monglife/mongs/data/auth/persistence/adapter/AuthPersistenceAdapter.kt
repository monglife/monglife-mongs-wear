package com.monglife.mongs.data.auth.persistence.adapter

import com.monglife.mongs.data.core.persistence.datastore.SessionDataStore
import com.monglife.mongs.data.core.persistence.entity.SessionEntity
import com.monglife.mongs.domain.auth.model.Session
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthPersistenceAdapterModule {
    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForAuthApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForDeviceApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.device.port.persistence.AuthPersistencePort
    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForPlayerApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort
}

class AuthPersistenceAdapter @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) : com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort,
    com.monglife.mongs.application.device.port.persistence.AuthPersistencePort,
    com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort {

    /**
     * 세션 존재 여부 조회
     */
    override suspend fun isExistsSession(): Boolean =
        sessionDataStore.getSession() != null

    /**
     * 세션 존재 여부 라이브 객체 조회
     */
    override suspend fun isExistsSessionFlow(): Flow<Boolean> =
        sessionDataStore.getSessionFlow().map { it != null }

    /**
     * 세션 조회
     */
    override suspend fun getSession(): Session? = sessionDataStore.getSession()?.toDomain()

    /**
     * 세션 저장
     */
    override suspend fun saveSession(session: Session): Session =
        sessionDataStore.saveSession(
            sessionEntity = SessionEntity(
                accountId = session.accountId,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                version = session.version,
            )
        ).toDomain()

    /**
     * 세션 삭제
     */
    override suspend fun deleteSession() {
        sessionDataStore.deleteSession()
    }
}
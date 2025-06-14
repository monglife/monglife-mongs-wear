package com.monglife.mongs.data.auth.persistence.adapter

import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthPersistenceAdapter @Inject constructor(

) : com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort,
    com.monglife.mongs.application.device.port.persistence.AuthPersistencePort,
    com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort,
    com.monglife.mongs.application.mong.port.persistence.AuthPersistencePort {

    override suspend fun isExistsSession(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isExistsSessionFlow(): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun saveSession(session: Session): Session {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSession(): Session {
        TODO("Not yet implemented")
    }
}
package com.monglife.mongs.application.auth.port.persistence

import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.flow.Flow

interface AuthPersistencePort {

    /**
     * 로그인 여부 라이브 객체 조회
     */
    suspend fun isExistsSessionFlow(): Flow<Boolean>

    /**
     * 세션 저장
     */
    suspend fun saveSession(session: Session): Session

    /**
     * 세션 삭제
     */
    suspend fun deleteSession() : Session
}

package com.monglife.mongs.application.member.player.port.persistence

import com.monglife.mongs.domain.auth.model.Session

interface AuthPersistencePort {

    /**
     * 로그인 여부 조회
     */
    suspend fun isExistsSession() : Boolean

    /**
     * 세션 조회
     */
    suspend fun getSession(): Session?
}
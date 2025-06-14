package com.monglife.mongs.application.member.player.port.persistence

interface AuthPersistencePort {

    /**
     * 로그인 여부 조회
     */
    suspend fun isExistsSession() : Boolean
}
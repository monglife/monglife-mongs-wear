package com.monglife.mongs.domain.member.player.repository

interface AuthRepository {

    /**
     * 로그인 여부 조회
     */
    suspend fun isLogin() : Boolean
}
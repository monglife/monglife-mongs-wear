package com.monglife.mongs.application.auth.port.web

import com.monglife.mongs.application.auth.exception.InvalidCreatePlayerException
import com.monglife.mongs.application.auth.exception.NotFoundPlayerException
import com.monglife.mongs.domain.member.player.model.Player

interface PlayerWebPort {

    /**
     * 플레이어 조회
     */
    @Throws(NotFoundPlayerException::class)
    suspend fun getPlayer(accountId: Long): GetPlayerResponse

    /**
     * 플레이어 등록
     */
    @Throws(InvalidCreatePlayerException::class)
    suspend fun createPlayer()
}
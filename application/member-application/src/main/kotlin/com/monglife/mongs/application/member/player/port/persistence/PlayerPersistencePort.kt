package com.monglife.mongs.application.member.player.port.persistence

import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerPersistencePort {

    /**
     * 플레이어 조회
     */
    @Throws(NotFoundPlayerException::class)
    suspend fun getPlayer(): Player

    /**
     * 플레이어 라이브 객체 조회
     */
    @Throws(NotFoundPlayerException::class)
    suspend fun getPlayerFlow(): Flow<Player>

    /**
     * 플레이어 로컬 동기화
     */
    suspend fun savePlayer(player: Player): Player
}
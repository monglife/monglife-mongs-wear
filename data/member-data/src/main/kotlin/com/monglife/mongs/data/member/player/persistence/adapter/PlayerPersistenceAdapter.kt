package com.monglife.mongs.data.member.player.persistence.adapter

import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerPersistenceAdapter @Inject constructor(

) : PlayerPersistencePort {

    /**
     * 플레이어 조회
     */
    @Throws(NotFoundPlayerException::class)
    override suspend fun getPlayer(): Player {
        TODO("Not yet implemented")
    }

    /**
     * 플레이어 라이브 객체 조회
     */
    @Throws(NotFoundPlayerException::class)
    override suspend fun getPlayerFlow(): Flow<Player> {
        TODO("Not yet implemented")
    }

    /**
     * 플레이어 로컬 동기화
     */
    override suspend fun savePlayer(player: Player): Player {
        TODO("Not yet implemented")
    }
}
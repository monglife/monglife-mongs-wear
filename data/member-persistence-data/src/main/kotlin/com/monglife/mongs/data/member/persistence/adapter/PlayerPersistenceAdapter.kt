package com.monglife.mongs.data.member.persistence.adapter

import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayerPersistenceAdapter @Inject constructor(

) : PlayerPersistencePort {

    override suspend fun getPlayer(): Player {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayerFlow(): Flow<Player> {
        TODO("Not yet implemented")
    }

    override suspend fun savePlayer(player: Player): Player {
        TODO("Not yet implemented")
    }
}
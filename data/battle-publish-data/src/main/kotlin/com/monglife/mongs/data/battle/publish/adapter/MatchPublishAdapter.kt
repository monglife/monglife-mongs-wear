package com.monglife.mongs.data.battle.publish.adapter

import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import javax.inject.Inject

class MatchPublishAdapter @Inject constructor(

) : MatchPublishPort {

    override suspend fun publishMatchEnter(matchId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun publishMatchExit(matchId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun publishMatchPick(
        matchId: Long,
        playerId: String,
        targetPlayerId: String,
        pickCode: String
    ) {
        TODO("Not yet implemented")
    }
}
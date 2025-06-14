package com.monglife.mongs.data.battle.subscribe.adapter

import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import javax.inject.Inject

class MatchSubScribeAdapter @Inject constructor(

) : MatchSubscribePort {

    override suspend fun subscribeMatch(matchId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun disSubscribeMatch(matchId: Long) {
        TODO("Not yet implemented")
    }
}
package com.monglife.mongs.data.battle.web.adapter

import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.port.web.response.GetBattleRewardResponse
import com.monglife.mongs.application.battle.port.web.response.GetMatchResponse
import com.monglife.mongs.application.battle.port.web.response.GetWinnerMatchPlayerResponse
import javax.inject.Inject

class MatchWebAdapter @Inject constructor(

) : MatchWebPort {

    override suspend fun getBattleReward(): GetBattleRewardResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getMatch(): GetMatchResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getWinnerMatchPlayer(matchId: Long): GetWinnerMatchPlayerResponse {
        TODO("Not yet implemented")
    }
}
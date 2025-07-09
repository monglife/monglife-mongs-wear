package com.monglife.mongs.data.battle.web.adapter

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchRewardException
import com.monglife.mongs.application.battle.exception.NotFoundWinnerMatchPlayerException
import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.port.web.response.GetBattleRewardResponse
import com.monglife.mongs.application.battle.port.web.response.GetMatchResponse
import com.monglife.mongs.application.battle.port.web.response.GetWinnerMatchPlayerResponse
import com.monglife.mongs.data.battle.web.client.MatchWebClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchWebAdapter @Inject constructor(
    private val matchWebClient: MatchWebClient,
) : MatchWebPort {

    /**
     * 배틀 보상 정보 조회
     */
    @Throws(NotFoundMatchRewardException::class)
    override suspend fun getBattleReward(): GetBattleRewardResponse =
        matchWebClient.getMatchOutcome().let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw NotFoundMatchRewardException()

            GetBattleRewardResponse(
                rewardPayPoint = body.result.rewardPayPoint,
                battlePayPoint = body.result.battingPayPoint,
            )
        }

    /**
     * 매치 조회
     */
    @Throws(NotFoundMatchException::class)
    override suspend fun getMatch(matchId: Long): GetMatchResponse {
        TODO("Not yet implemented")
    }

    /**
     * 승리 매치 플레이어 조회
     */
    @Throws(NotFoundWinnerMatchPlayerException::class)
    override suspend fun getWinnerMatchPlayer(matchId: Long): GetWinnerMatchPlayerResponse {
        TODO("Not yet implemented")
    }
}
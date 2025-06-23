package com.monglife.mongs.application.battle.port.web

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchRewardException
import com.monglife.mongs.application.battle.exception.NotFoundWinnerMatchPlayerException
import com.monglife.mongs.application.battle.port.web.response.GetBattleRewardResponse
import com.monglife.mongs.application.battle.port.web.response.GetMatchResponse
import com.monglife.mongs.application.battle.port.web.response.GetWinnerMatchPlayerResponse

interface MatchWebPort {

    /**
     * 배틀 보상 정보 조회
     */
    @Throws(NotFoundMatchRewardException::class)
    suspend fun getBattleReward(): GetBattleRewardResponse

    /**
     * 매치 조회
     */
    @Throws(NotFoundMatchException::class)
    suspend fun getMatch(matchId: Long): GetMatchResponse

    /**
     * 승리 매치 플레이어 조회
     */
    @Throws(NotFoundWinnerMatchPlayerException::class)
    suspend fun getWinnerMatchPlayer(matchId: Long): GetWinnerMatchPlayerResponse
}
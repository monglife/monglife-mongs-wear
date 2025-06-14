package com.monglife.mongs.application.battle.port.publish

import com.monglife.mongs.application.battle.exception.InvalidPublishMatchEnterException
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchExitException
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchPickException

interface MatchPublishPort {

    /**
     * 매치 입장
     */
    @Throws(InvalidPublishMatchEnterException::class)
    suspend fun publishMatchEnter(matchId: Long)

    /**
     * 매치 퇴장
     */
    @Throws(InvalidPublishMatchExitException::class)
    suspend fun publishMatchExit(matchId: Long)

    /**
     * 매치 선택
     */
    @Throws(InvalidPublishMatchPickException::class)
    suspend fun publishMatchPick(matchId: Long, playerId: String, targetPlayerId: String, pickCode: String)
}
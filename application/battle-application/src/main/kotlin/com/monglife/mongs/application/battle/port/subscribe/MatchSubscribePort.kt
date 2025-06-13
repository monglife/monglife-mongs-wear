package com.monglife.mongs.application.battle.port.subscribe

import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeMatchException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeMatchException

interface MatchSubscribePort {

    /**
     * 매치 구독
     */
    @Throws(InvalidSubscribeMatchException::class)
    suspend fun subscribeMatch(matchId: Long)

    /**
     * 매치 구독 해제
     */
    @Throws(InvalidDisSubscribeMatchException::class)
    suspend fun disSubscribeMatch(matchId: Long)
}
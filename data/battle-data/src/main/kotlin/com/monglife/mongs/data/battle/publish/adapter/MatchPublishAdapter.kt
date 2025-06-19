package com.monglife.mongs.data.battle.publish.adapter

import android.util.Log
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchEnterException
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchExitException
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchPickException
import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchPublishAdapter @Inject constructor(

) : MatchPublishPort {

    /**
     * 매치 입장
     */
    @Throws(InvalidPublishMatchEnterException::class)
    override suspend fun publishMatchEnter(matchId: Long) {
        Log.d("TEST", "publishMatchEnter")
    }

    /**
     * 매치 퇴장
     */
    @Throws(InvalidPublishMatchExitException::class)
    override suspend fun publishMatchExit(matchId: Long) {
        Log.d("TEST", "publishMatchExit")
    }

    /**
     * 매치 선택
     */
    @Throws(InvalidPublishMatchPickException::class)
    override suspend fun publishMatchPick(
        matchId: Long,
        playerId: String,
        targetPlayerId: String,
        pickCode: String
    ) {
        Log.d("TEST", "publishMatchPick")
    }
}
package com.monglife.mongs.data.battle.subscribe.adapter

import android.util.Log
import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeMatchException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeMatchException
import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import com.monglife.mongs.application.battle.port.subscribe.event.MatchEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchSubscribeAdapter @Inject constructor(

) : MatchSubscribePort {

    /**
     * 매치 구독
     */
    @Throws(InvalidSubscribeMatchException::class)
    override suspend fun subscribeMatch(matchId: Long): Flow<MatchEvent> {
        Log.d("TEST", "subscribeMatch")
        return flow {  }
    }

    /**
     * 매치 구독 해제
     */
    @Throws(InvalidDisSubscribeMatchException::class)
    override suspend fun disSubscribeMatch(matchId: Long) {
        Log.d("TEST", "disSubscribeMatch")
    }
}
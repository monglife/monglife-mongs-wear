package com.monglife.mongs.data.battle.subscribe.adapter

import android.util.Log
import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeMatchException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeMatchException
import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import com.monglife.mongs.application.battle.port.subscribe.event.MatchEvent
import com.monglife.mongs.application.battle.port.subscribe.event.MatchEventCode
import com.monglife.mongs.application.battle.port.subscribe.event.MatchPlayerVo
import com.monglife.mongs.application.battle.port.subscribe.event.MatchStartEvent
import com.monglife.mongs.domain.battle.enums.MatchRoundCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class MatchSubscribeAdapter @Inject constructor(
    // TODO: mqtt 클라이언트 연결
) : MatchSubscribePort {

    /**
     * 매치 구독
     */
    @Throws(InvalidSubscribeMatchException::class)
    override suspend fun subscribeMatch(matchId: Long): Flow<MatchEvent> {
        Log.d("TEST", "subscribe match")
        return flow {
            delay(5000)
            Log.d("TEST", "match start!")
            MatchStartEvent(
                code = MatchEventCode.MATCH_PLAYERS_ENTERED,
                matchId = 1L,
                round = 1,
                isLastRound = false,
                matchPlayers = listOf(
                    MatchPlayerVo(
                        playerId = UUID.randomUUID().toString().replace("-", ""),
                        deviceId = UUID.randomUUID().toString().replace("-", ""),
                        mongCode = "CH100",
                        mongName = "별몽",
                        name = "테스트 몽 이름",
                        hp = 500.0,
                        roundCode = MatchRoundCode.NONE,
                    ),
                    MatchPlayerVo(
                        playerId = UUID.randomUUID().toString().replace("-", ""),
                        deviceId = UUID.randomUUID().toString().replace("-", ""),
                        mongCode = "CH101",
                        mongName = "네몽",
                        name = "테스트 봇 몽 이름",
                        hp = 500.0,
                        roundCode = MatchRoundCode.NONE,
                    )
                )
            )
        }
    }

    /**
     * 매치 구독 해제
     */
    @Throws(InvalidDisSubscribeMatchException::class)
    override suspend fun disSubscribeMatch(matchId: Long) {
        Log.d("TEST", "disSubscribe match")
    }
}
package com.monglife.mongs.data.battle.subscribe.adapter

import android.util.Log
import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeQueueException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeQueueException
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import com.monglife.mongs.application.battle.port.subscribe.event.MatchingEvent
import com.monglife.mongs.application.battle.port.subscribe.event.QueueEvent
import com.monglife.mongs.application.battle.port.subscribe.event.QueueEventCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class QueueSubscribeAdapter @Inject constructor(
    // TODO: mqtt 클라이언트 연결
) : QueueSubscribePort {

    /**
     * 매치 큐 구독
     */
    @Throws(InvalidSubscribeQueueException::class)
    override suspend fun subscribeQueue(deviceId: String): Flow<QueueEvent> {
        Log.d("TEST", "subscribe queue")
        return flow {
            delay(5000)
            Log.d("TEST", "matching!")
            MatchingEvent(
                code = QueueEventCode.MATCHING_QUEUE_PLAYER,
                matchId = 1L,
                matchPlayers = listOf(
                    MatchingEvent.QueuePlayerVo(
                        deviceId = deviceId,
                        playerId = UUID.randomUUID().toString().replace("-", ""),
                        mongId = 1L,
                        mongCode = "CH100",
                        mongName = "별몽",
                        name = "테스트 몽 이름"
                    ),
                    MatchingEvent.QueuePlayerVo(
                        deviceId = UUID.randomUUID().toString().replace("-", ""),
                        playerId = UUID.randomUUID().toString().replace("-", ""),
                        mongId = 1L,
                        mongCode = "CH101",
                        mongName = "네몽",
                        name = "테스트 봇 몽 이름"
                    )
                )
            )
        }
    }

    /**
     * 매치 큐 구독 해제
     */
    @Throws(InvalidDisSubscribeQueueException::class)
    override suspend fun disSubscribeQueue(deviceId: String) {
        Log.d("TEST", "disSubscribe queue")
    }
}
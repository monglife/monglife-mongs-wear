package com.monglife.mongs.application.battle.port.subscribe

import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeQueueException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeQueueException
import com.monglife.mongs.application.battle.port.subscribe.event.QueueEvent
import kotlinx.coroutines.flow.Flow

interface QueueSubscribePort {

    /**
     * 매치 큐 구독
     */
    @Throws(InvalidSubscribeQueueException::class)
    suspend fun subscribeQueue(deviceId: String): Flow<QueueEvent>

    /**
     * 매치 큐 구독 해제
     */
    @Throws(InvalidDisSubscribeQueueException::class)
    suspend fun disSubscribeQueue(deviceId: String)
}
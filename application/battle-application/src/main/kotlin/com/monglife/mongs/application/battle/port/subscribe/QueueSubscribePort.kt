package com.monglife.mongs.application.battle.port.subscribe

import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeQueueException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeQueueException

interface QueueSubscribePort {

    /**
     * 매치 큐 구독
     */
    @Throws(InvalidSubscribeQueueException::class)
    suspend fun subscribeQueue(deviceId: String)

    /**
     * 매치 큐 구독 해제
     */
    @Throws(InvalidDisSubscribeQueueException::class)
    suspend fun disSubscribeQueue(deviceId: String)
}
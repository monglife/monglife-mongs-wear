package com.monglife.mongs.data.battle.subscribe.adapter

import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeQueueException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeQueueException
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import com.monglife.mongs.application.battle.port.subscribe.event.UpdateQueueEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueSubscribeAdapter @Inject constructor(

) : QueueSubscribePort {

    /**
     * 매치 큐 구독
     */
    @Throws(InvalidSubscribeQueueException::class)
    override suspend fun subscribeQueue(deviceId: String): Flow<UpdateQueueEvent> {
        TODO("Not yet implemented")
    }

    /**
     * 매치 큐 구독 해제
     */
    @Throws(InvalidDisSubscribeQueueException::class)
    override suspend fun disSubscribeQueue(deviceId: String) {
        TODO("Not yet implemented")
    }
}
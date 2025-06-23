package com.monglife.mongs.application.mong.port.subscribe

import com.monglife.mongs.application.mong.exception.InvalidDisSubscribeMongException
import com.monglife.mongs.application.mong.exception.InvalidSubscribeMongException
import com.monglife.mongs.application.mong.port.subscribe.event.ManagementEvent
import kotlinx.coroutines.flow.Flow

interface ManagementSubscribePort {

    /**
     * 몽 구독
     */
    @Throws(InvalidSubscribeMongException::class)
    suspend fun subscribeMong(mongId: Long): Flow<ManagementEvent>

    /**
     * 몽 구독 해제
     */
    @Throws(InvalidDisSubscribeMongException::class)
    suspend fun disSubscribeMong(mongId: Long)
}
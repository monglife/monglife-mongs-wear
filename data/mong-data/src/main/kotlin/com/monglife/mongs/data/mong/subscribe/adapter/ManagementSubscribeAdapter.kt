package com.monglife.mongs.data.mong.subscribe.adapter

import android.util.Log
import com.monglife.mongs.application.mong.exception.InvalidDisSubscribeMongException
import com.monglife.mongs.application.mong.exception.InvalidSubscribeMongException
import com.monglife.mongs.application.mong.port.subscribe.ManagementSubscribePort
import com.monglife.mongs.application.mong.port.subscribe.event.ManagementEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementSubscribeAdapter @Inject constructor(

) : ManagementSubscribePort {

    /**
     * 몽 구독
     */
    @Throws(InvalidSubscribeMongException::class)
    override suspend fun subscribeMong(mongId: Long): Flow<ManagementEvent> {
        Log.d("TEST", "subscribe mong")
        return flow {  }
    }

    /**
     * 몽 구독 해제
     */
    @Throws(InvalidDisSubscribeMongException::class)
    override suspend fun disSubscribeMong(mongId: Long) {
        Log.d("TEST", "disSubscribe mong")
    }
}
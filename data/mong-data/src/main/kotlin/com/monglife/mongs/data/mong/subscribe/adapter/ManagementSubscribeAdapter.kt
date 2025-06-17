package com.monglife.mongs.data.mong.subscribe.adapter

import com.monglife.mongs.application.mong.exception.InvalidDisSubscribeMongException
import com.monglife.mongs.application.mong.exception.InvalidSubscribeMongException
import com.monglife.mongs.application.mong.port.subscribe.ManagementSubscribePort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementSubscribeAdapter @Inject constructor(

) : ManagementSubscribePort {

    /**
     * 몽 구독
     */
    @Throws(InvalidSubscribeMongException::class)
    override suspend fun subscribeMong(mongId: Long) {
        TODO("Not yet implemented")
    }

    /**
     * 몽 구독 해제
     */
    @Throws(InvalidDisSubscribeMongException::class)
    override suspend fun disSubscribeMong(mongId: Long) {
        TODO("Not yet implemented")
    }
}
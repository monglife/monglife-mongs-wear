package com.monglife.mongs.data.mong.subscribe.adapter

import com.monglife.mongs.application.mong.port.subscribe.ManagementSubscribePort
import javax.inject.Inject

class ManagementSubscribeAdapter @Inject constructor(

) : ManagementSubscribePort {

    override suspend fun subscribeMong(mongId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun disSubscribeMong(mongId: Long) {
        TODO("Not yet implemented")
    }
}
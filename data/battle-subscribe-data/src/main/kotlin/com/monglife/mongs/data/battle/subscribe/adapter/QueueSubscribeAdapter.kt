package com.monglife.mongs.data.battle.subscribe.adapter

import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import javax.inject.Inject

class QueueSubscribeAdapter @Inject constructor(

) : QueueSubscribePort {

    override suspend fun subscribeQueue(deviceId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun disSubscribeQueue(deviceId: String) {
        TODO("Not yet implemented")
    }
}
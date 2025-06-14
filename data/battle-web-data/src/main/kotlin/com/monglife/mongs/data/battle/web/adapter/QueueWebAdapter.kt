package com.monglife.mongs.data.battle.web.adapter

import com.monglife.mongs.application.battle.port.web.QueueWebPort
import javax.inject.Inject

class QueueWebAdapter @Inject constructor(

) : QueueWebPort {

    override suspend fun createQueuePlayer(mongId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteQueuePlayer(mongId: Long) {
        TODO("Not yet implemented")
    }
}
package com.monglife.mongs.data.battle.web.adapter

import com.monglife.mongs.application.battle.exception.InvalidCreateQueuePlayerException
import com.monglife.mongs.application.battle.exception.InvalidDeleteQueuePlayerException
import com.monglife.mongs.application.battle.port.web.QueueWebPort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueWebAdapter @Inject constructor(

) : QueueWebPort {

    /**
     * 매치 큐 등록
     */
    @Throws(InvalidCreateQueuePlayerException::class)
    override suspend fun createQueuePlayer(mongId: Long) {
        TODO("Not yet implemented")
    }

    /**
     * 매치 큐 등록
     */
    @Throws(InvalidDeleteQueuePlayerException::class)
    override suspend fun deleteQueuePlayer(mongId: Long) {
        TODO("Not yet implemented")
    }
}
package com.monglife.mongs.application.battle.port.web

import com.monglife.mongs.application.battle.exception.InvalidCreateQueuePlayerException
import com.monglife.mongs.application.battle.exception.InvalidDeleteQueuePlayerException

interface MatchQueueWebPort {

    /**
     * 매치 큐 등록
     */
    @Throws(InvalidCreateQueuePlayerException::class)
    suspend fun createQueuePlayer(mongId: Long, deviceId: String)

    /**
     * 매치 큐 등록
     */
    @Throws(InvalidDeleteQueuePlayerException::class)
    suspend fun deleteQueuePlayer(mongId: Long)
}
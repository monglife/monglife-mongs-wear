package com.monglife.mongs.data.battle.web.adapter

import com.monglife.mongs.application.battle.exception.InvalidCreateQueuePlayerException
import com.monglife.mongs.application.battle.exception.InvalidDeleteQueuePlayerException
import com.monglife.mongs.application.battle.port.web.MatchQueueWebPort
import com.monglife.mongs.data.battle.web.client.MatchQueueWebClient
import javax.inject.Inject

class MatchQueueWebAdapter @Inject constructor(
    private val matchQueueWebClient: MatchQueueWebClient,
) : MatchQueueWebPort {

    /**
     * 매치 큐 등록
     */
    @Throws(InvalidCreateQueuePlayerException::class)
    override suspend fun createQueuePlayer(mongId: Long, deviceId: String) {
        matchQueueWebClient.createQueuePlayer(mongId = mongId).let { response ->
            response.takeIf { it.isSuccessful } ?: throw InvalidCreateQueuePlayerException()
        }
    }

    /**
     * 매치 큐 삭제
     */
    @Throws(InvalidDeleteQueuePlayerException::class)
    override suspend fun deleteQueuePlayer(mongId: Long) {
        return matchQueueWebClient.deleteQueuePlayer(mongId = mongId).let { response ->
            response.takeIf { it.isSuccessful } ?: throw InvalidDeleteQueuePlayerException()
        }
    }
}
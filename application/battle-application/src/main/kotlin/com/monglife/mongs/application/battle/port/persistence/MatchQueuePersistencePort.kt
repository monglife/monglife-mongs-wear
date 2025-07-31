package com.monglife.mongs.application.battle.port.persistence

import com.monglife.mongs.domain.battle.model.MatchQueue
import kotlinx.coroutines.flow.Flow

interface MatchQueuePersistencePort {

    /**
     * 매치 큐 Flow 조회
     */
    suspend fun createMatchQueue(mongId: Long, deviceId: String): Flow<MatchQueue?>
}
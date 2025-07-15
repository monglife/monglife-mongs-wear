package com.monglife.mongs.application.battle.port.persistence

import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchPersistencePort {

    /**
     * 매치 라이브 객체 조회
     */
    suspend fun getMatchFlow(matchId: Long): Flow<Match?>
}
package com.monglife.mongs.data.battle.persistence.adapter

import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MatchPersistenceAdapter @Inject constructor(

) : MatchPersistencePort {

    override suspend fun getMatch(deviceId: String): Match {
        TODO("Not yet implemented")
    }

    override suspend fun getMatch(matchId: Long): Match {
        TODO("Not yet implemented")
    }

    override suspend fun getMatchFlow(deviceId: String): Flow<Match> {
        TODO("Not yet implemented")
    }

    override suspend fun getMatchFlow(matchId: Long): Flow<Match> {
        TODO("Not yet implemented")
    }

    override suspend fun getMatchPlayersFlow(matchId: Long): List<Flow<MatchPlayer>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveMatch(match: Match): Match {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMatch(matchId: Long): Match {
        TODO("Not yet implemented")
    }
}
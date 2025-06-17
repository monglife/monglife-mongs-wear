package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 최근 배틀 정보 조회 UseCase
 */
class ObserveLeastMatchUseCase @Inject constructor(
    private val matchPersistencePort: MatchPersistencePort,
) : BaseNoParamUseCase<Flow<MatchVo?>>() {

    @Throws(NotFoundMatchException::class)
    override suspend fun execute(): Flow<MatchVo?> {
        return withContext(Dispatchers.IO) {
            // 매치 라이브 객체 로컬 조회
            val matchId = matchPersistencePort.getLeastMatch().matchId

            val matchFlow = matchPersistencePort.getMatchFlow(matchId = matchId)
            val matchPlayersFlow = matchPersistencePort.getMatchPlayersFlow(matchId = matchId)
            combine(matchFlow, matchPlayersFlow) { match, matchPlayers ->
                match?.let { MatchVo.of(match = match, matchPlayers = matchPlayers) }
            }
        }
    }
}
package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 최근 배틀 정보 조회 UseCase
 */
class ObserveLeastMatchUseCase @Inject constructor(
    private val matchWebPort: MatchWebPort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseNoParamUseCase<Flow<MatchVo?>>() {

    @Throws(NotFoundMatchException::class)
    override suspend fun execute(): Flow<MatchVo?> {
        return withContext(Dispatchers.IO) {
            // 매치 라이브 객체 로컬 조회
            val matchId = matchPersistencePort.getLeastMatch().matchId

            // 매치 정보 조회 후 업데이트
            matchWebPort.getMatch(matchId = matchId).let { response ->
                // 매치 플레이어 업데이트
                response.matchPlayers.forEach {
                    matchPersistencePort.getMatchPlayer(playerId = it.playerId).let { matchPlayer: MatchPlayer ->
                        matchPlayer.update(
                            hp = it.hp,
                            roundCode = it.roundCode,
                        )
                        matchPersistencePort.saveMatchPlayer(matchPlayer = matchPlayer)
                    }
                }
                // 매치 업데이트
                matchPersistencePort.getMatch(matchId = matchId).let { match: Match ->
                    match.update(
                        round = response.round,
                        isLastRound = response.isLastRound,
                    )
                    matchPersistencePort.saveMatch(match = match)
                }
            }

            // 매치 라이브 객체 반환
            combine(
                matchPersistencePort.getMatchFlow(matchId = matchId),
                matchPersistencePort.getMatchPlayersFlow(matchId = matchId)
            ) { match, matchPlayers ->
                match?.let { MatchVo.of(match = match, matchPlayers = matchPlayers) }
            }
        }
    }
}
package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 시작 UseCase
 */
class MatchStartUseCase @Inject constructor(
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<MatchStartUseCase.Command, MatchVo>() {

    @Throws(NotFoundMatchException::class)
    override suspend fun execute(command: Command): MatchVo {
        return withContext(Dispatchers.IO) {
            // 매치 로컬 조회
            matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                // 매치 시작 변경
                match.start()
                // 매치 로컬 저장
                matchPersistencePort.saveMatch(match = match)
                // MatchVo 반환
                val matchPlayers = matchPersistencePort.getMatchPlayers(matchId = match.matchId)
                MatchVo.of(match = match, matchPlayers = matchPlayers)
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
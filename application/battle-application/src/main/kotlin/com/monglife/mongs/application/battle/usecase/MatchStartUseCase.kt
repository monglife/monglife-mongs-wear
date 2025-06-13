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
            matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                match.start()
                matchPersistencePort.saveMatch(match = match)
                // MatchVo 반환
                MatchVo.of(match = match)
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
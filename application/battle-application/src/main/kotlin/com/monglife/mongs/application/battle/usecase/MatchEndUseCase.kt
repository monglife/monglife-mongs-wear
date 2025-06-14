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
 * 매치 종료 UseCase
 */
class MatchEndUseCase @Inject constructor(
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<MatchEndUseCase.Command, MatchVo>() {

    @Throws(NotFoundMatchException::class)
    override suspend fun execute(command: Command): MatchVo {
        return withContext(Dispatchers.IO) {
            // 매치 로컬 조회
            matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                // 매치 로컬 삭제
                matchPersistencePort.deleteMatch(matchId = match.matchId)
                // MatchVo 반환
                MatchVo.of(match = match)
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
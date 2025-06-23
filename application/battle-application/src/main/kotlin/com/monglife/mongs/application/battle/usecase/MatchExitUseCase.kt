package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidPublishMatchExitException
import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 중도 퇴장 UseCase
 */
class MatchExitUseCase @Inject constructor(
    private val matchPersistencePort: MatchPersistencePort,
    private val matchPublishPort: MatchPublishPort,
) : BaseParamUseCase<MatchExitUseCase.Command, Unit>() {

    @Throws(NotFoundMatchException::class, InvalidPublishMatchExitException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 로컬 조회
            matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                // 매치 삭제
                matchPersistencePort.deleteAllMatch()
                // 배틀 퇴장 이벤트 전송
                matchPublishPort.publishMatchExit(matchId = match.matchId)
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
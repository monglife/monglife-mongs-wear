package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidPublishMatchEnterException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchPlayerException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 입장 UseCase
 */
class MatchEnterUseCase @Inject constructor(
    private val matchPublishPort: MatchPublishPort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<MatchEnterUseCase.Command, Unit>() {

    @Throws(NotFoundMatchException::class, InvalidPublishMatchEnterException::class, NotFoundMatchPlayerException::class, InvalidSubscribeMatchException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 로컬 조회
            matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                // 매치 입장 이벤트 전송
                matchPublishPort.publishMatchEnter(matchId = match.matchId)
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
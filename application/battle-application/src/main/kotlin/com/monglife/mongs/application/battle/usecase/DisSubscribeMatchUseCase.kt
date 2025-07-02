package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidPublishMatchEnterException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchPlayerException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 구독 해제 UseCase
 */
class DisSubscribeMatchUseCase @Inject constructor(
    private val matchSubscribePort: MatchSubscribePort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<DisSubscribeMatchUseCase.Command, Unit>() {

    @Throws(NotFoundMatchException::class, InvalidPublishMatchEnterException::class, NotFoundMatchPlayerException::class, InvalidSubscribeMatchException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 로컬 조회
            matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                // 매치 구독 해제
                matchSubscribePort.disSubscribeMatch(matchId = match.matchId)
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
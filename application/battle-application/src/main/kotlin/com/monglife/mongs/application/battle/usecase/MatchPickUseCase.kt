package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidPublishMatchPickException
import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.enums.MatchPickCode
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 선택 UseCase
 */
class MatchPickUseCase @Inject constructor(
    private val matchPersistencePort: MatchPersistencePort,
    private val matchPublishPort: MatchPublishPort,
) : BaseParamUseCase<MatchPickUseCase.Command, Unit>() {

    @Throws(NotFoundMatchException::class, InvalidPublishMatchPickException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 로컬 조회
            matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                // 매치 선택 이벤트 전송
                matchPublishPort.publishMatchPick(
                    matchId = command.matchId,
                    playerId = command.playerId,
                    targetPlayerId = command.targetPlayerId,
                    pickCode = command.pickCode.name
                )
            }
        }
    }

    data class Command(
        val matchId: Long,
        val playerId: String,
        val targetPlayerId: String,
        val pickCode: MatchPickCode,
    )
}
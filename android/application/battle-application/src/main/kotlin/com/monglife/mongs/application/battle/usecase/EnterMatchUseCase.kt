package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchEnterException
import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import javax.inject.Inject

/**
 * 매치 입장 UseCase
 */
class EnterMatchUseCase @Inject constructor(
    private val matchPublishPort: MatchPublishPort,
) : BaseParamUseCase<EnterMatchUseCase.Command, Unit>() {

    @Throws(InvalidPublishMatchEnterException::class)
    override suspend fun execute(command: Command) {
        matchPublishPort.publishMatchEnter(
            matchId = command.matchId,
            playerId = command.playerId,
        )
    }

    data class Command(
        val matchId: Long,
        val playerId: String,
    )
}
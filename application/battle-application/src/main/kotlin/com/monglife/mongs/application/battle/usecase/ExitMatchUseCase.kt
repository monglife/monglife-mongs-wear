package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchExitException
import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 퇴장 UseCase
 */
class ExitMatchUseCase @Inject constructor(
    private val matchPublishPort: MatchPublishPort,
) : BaseParamUseCase<ExitMatchUseCase.Command, Unit>() {

    @Throws(InvalidPublishMatchExitException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            matchPublishPort.publishMatchExit(
                matchId = command.matchId,
                playerId = command.playerId,
            )
        }
    }

    data class Command(
        val matchId: Long,
        val playerId: String,
    )
}
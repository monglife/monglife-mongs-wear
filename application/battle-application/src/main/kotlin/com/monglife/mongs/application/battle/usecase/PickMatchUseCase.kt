package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.exception.InvalidPublishMatchPickException
import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import com.monglife.mongs.domain.battle.enums.MatchPickCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 선택 UseCase
 */
class PickMatchUseCase @Inject constructor(
    private val matchPublishPort: MatchPublishPort,
) : BaseParamUseCase<PickMatchUseCase.Command, Unit>() {

    @Throws(InvalidPublishMatchPickException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            matchPublishPort.publishMatchPick(
                matchId = command.matchId,
                playerId = command.playerId,
                targetPlayerId = command.targetPlayerId,
                pickCode = command.pickCode.name,
            )
        }
    }

    data class Command(
        val matchId: Long,
        val playerId: String,
        val targetPlayerId: String,
        val pickCode: MatchPickCode,
    )
}
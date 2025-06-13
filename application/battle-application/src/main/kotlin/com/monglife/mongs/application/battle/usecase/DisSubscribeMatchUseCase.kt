package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeMatchException
import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 구독 해제 UseCase
 */
class DisSubscribeMatchUseCase @Inject constructor(
    private val matchSubscribePort: MatchSubscribePort,
) : BaseParamUseCase<DisSubscribeMatchUseCase.Command, Unit>() {

    @Throws(InvalidDisSubscribeMatchException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 구독 해제
            matchSubscribePort.disSubscribeMatch(matchId = command.matchId)
        }
    }

    data class Command(
        val matchId: Long,
    )
}
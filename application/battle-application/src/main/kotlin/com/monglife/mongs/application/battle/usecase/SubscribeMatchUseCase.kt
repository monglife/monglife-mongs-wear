package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidSubscribeMatchException
import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 구독 UseCase
 */
class SubscribeMatchUseCase @Inject constructor(
    private val matchSubscribePort: MatchSubscribePort,
) : BaseParamUseCase<SubscribeMatchUseCase.Command, Unit>() {

    @Throws(InvalidSubscribeMatchException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 구독
            matchSubscribePort.subscribeMatch(matchId = command.matchId)
        }
    }

    data class Command(
        val matchId: Long,
    )
}
package com.monglife.mongs.application.member.player.usecase

import com.monglife.mongs.application.member.player.port.subscribe.PlayerSubscribePort
import com.monglife.mongs.application.member.store.exception.InvalidSubscribePlayerException
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 플레이어 구독 UseCase
 */
class SubscribePlayerUseCase @Inject constructor(
    private val playerSubscribePort: PlayerSubscribePort,
) : BaseParamUseCase<SubscribePlayerUseCase.Command, Unit>() {

    @Throws(InvalidSubscribePlayerException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // Player 구독
            playerSubscribePort.subscribePlayer(accountId = command.accountId)
        }
    }

    data class Command(
        val accountId: Long,
    )
}
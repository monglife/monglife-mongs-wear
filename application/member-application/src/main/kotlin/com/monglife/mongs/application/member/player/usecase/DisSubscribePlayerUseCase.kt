package com.monglife.mongs.application.member.player.usecase

import com.monglife.mongs.application.member.player.exception.InvalidDisSubscribePlayerException
import com.monglife.mongs.application.member.player.port.subscribe.PlayerSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 플레이어 구독 해제 UseCase
 */
class DisSubscribePlayerUseCase @Inject constructor(
    private val playerSubscribePort: PlayerSubscribePort,
) : BaseParamUseCase<DisSubscribePlayerUseCase.Command, Unit>() {

    @Throws(InvalidDisSubscribePlayerException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 플레이어 구독 해제
            playerSubscribePort.disSubscribePlayer(accountId = command.accountId)
        }
    }

    data class Command(
        val accountId: Long,
    )
}
package com.monglife.mongs.application.member.player.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.member.player.exception.InvalidExchangeStarPointException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.application.member.player.vo.PlayerVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 스타 포인트 환전 UseCase
 */
class ExchangeStarPointUseCase @Inject constructor(
    private val playerWebPort: PlayerWebPort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseParamUseCase<ExchangeStarPointUseCase.Command, Unit>() {

    @Throws(NotFoundPlayerException::class, InvalidExchangeStarPointException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            playerWebPort.getPlayer().let { response ->
                response.toDomain().let { player ->
                    // 스타 포인트 환전 요청
                    playerWebPort.exchangeStarPoint(
                        mongId = command.mongId,
                        starPoint = command.starPoint,
                    ).let { player.exchangeStarPoint(starPoint = it.starPoint) }

                    playerPersistencePort.savePlayer(player = player)

                    PlayerVo.of(player = player)
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
        val starPoint: Int,
    )
}
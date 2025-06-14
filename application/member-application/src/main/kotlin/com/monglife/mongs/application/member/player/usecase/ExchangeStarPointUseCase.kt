package com.monglife.mongs.application.member.player.usecase

import com.monglife.mongs.application.member.player.exception.InvalidExchangeStarPointException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.application.member.player.vo.PlayerVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
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
            // 플레이어 조회 요청
            playerWebPort.getPlayer().let { response ->
                val player = response.toDomain()
                // 스타 포인트 환전 요청
                playerWebPort.exchangeStarPoint(
                    mongId = command.mongId,
                    starPoint = command.starPoint,
                ).let {
                    // 플레이어 스타 포인트 환전
                    player.exchangeStarPoint(starPoint = it.starPoint)
                }
                // 플레이어 로컬 등록
                playerPersistencePort.savePlayer(player = player)
                // PlayerVo 반환
                PlayerVo.of(player = player)
            }
        }
    }

    data class Command(
        val mongId: Long,
        val starPoint: Int,
    )
}
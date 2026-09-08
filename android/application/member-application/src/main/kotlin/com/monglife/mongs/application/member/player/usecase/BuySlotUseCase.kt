package com.monglife.mongs.application.member.player.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.member.player.exception.InvalidBuySlotException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.application.member.player.vo.PlayerVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 슬롯 구매 UseCase
 */
class BuySlotUseCase @Inject constructor(
    private val playerWebPort: PlayerWebPort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseNoParamUseCase<PlayerVo>() {

    @Throws(NotFoundPlayerException::class, InvalidBuySlotException::class)
    override suspend fun execute(): PlayerVo {
        return withContext(Dispatchers.IO) {
            playerWebPort.getPlayer().let {
                it.toDomain().let { player ->
                    playerWebPort.buySlot().let { response ->
                        player.buySlot(
                            slotCount = response.slotCount,
                            starPoint = response.starPoint,
                        )
                    }

                    playerPersistencePort.savePlayer(player = player)

                    PlayerVo.of(player = player)
                }
            }
        }
    }
}
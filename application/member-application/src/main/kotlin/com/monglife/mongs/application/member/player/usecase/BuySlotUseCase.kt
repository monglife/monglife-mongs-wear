package com.monglife.mongs.application.member.player.usecase

import com.monglife.mongs.application.member.player.exception.InvalidBuySlotException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.application.member.player.vo.PlayerVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
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
            // 플레이어 조회 요청
            playerWebPort.getPlayer().let {
                val player = it.toDomain()
                // 슬롯 구매 요청
                playerWebPort.buySlot().let { response ->
                    // 플레이어 슬롯 구매 수정
                    player.buySlot(
                        slotCount = response.slotCount,
                        starPoint = response.starPoint,
                    )
                }
                // 플레이어 로컬 등록
                playerPersistencePort.savePlayer(player = player)
                // PlayerVo 반혼
                PlayerVo.of(player = player)
            }
        }
    }
}
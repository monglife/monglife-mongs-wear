package com.monglife.mongs.application.member.store.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.store.exception.InvalidConsumeOrderException
import com.monglife.mongs.application.member.store.port.web.StoreWebPort
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 인앱 상품 주문 소비 UseCase
 */
class ConsumeProductOrderUseCase @Inject constructor(
    private val storeWebPort: StoreWebPort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseParamUseCase<ConsumeProductOrderUseCase.Command, Unit>() {

    @Throws(NotFoundPlayerException::class, InvalidConsumeOrderException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 주문 소비 요청
            storeWebPort.consumeOrder(
                productId = command.productId,
                socialOrderId = command.socialOrderId,
                purchaseToken = command.purchaseToken,
            ).let { response ->
                // 플레이어 로컬 조회
                playerPersistencePort.getPlayer()?.let { player: Player ->
                    // 플레이어 업데이트
                    player.update(
                        slotCount = response.slotCount,
                        starPoint = response.starPoint,
                    )
                    // 플레이어 로컬 등록
                    playerPersistencePort.savePlayer(player = player)
                } ?: throw NotFoundPlayerException()
            }
        }
    }

    data class Command(
        val productId: String,
        val socialOrderId: String,
        val purchaseToken: String,
    )
}
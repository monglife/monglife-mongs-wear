package com.monglife.mongs.domain.member.player.usecase

import com.monglife.mongs.domain.member.player.repository.PlayerRepository
import com.mongs.wear.core.exception.data.BuySlotException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.BuySlotUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 슬롯 구매 UseCase
 */
class BuySlotUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            playerRepository.buySlot()
        }
    }

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is BuySlotException -> throw BuySlotUseCaseException()

            else -> throw BuySlotUseCaseException()
        }
    }
}
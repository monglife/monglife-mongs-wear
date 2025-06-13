package com.monglife.mongs.domain.member.player.usecase

import com.monglife.mongs.domain.member.player.repository.PlayerRepository
import com.mongs.wear.core.exception.data.GetPlayerException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.GetSlotCountUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 슬롯 수 조회 UseCase
 */
class GetSlotCountUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
) : BaseNoParamUseCase<Int>() {

    override suspend fun execute(): Int {
        return withContext(Dispatchers.IO) {
            playerRepository.getSlotCount()
        }
    }

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is GetPlayerException -> throw GetSlotCountUseCaseException()

            else -> throw GetSlotCountUseCaseException()
        }
    }
}
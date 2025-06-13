package com.monglife.mongs.domain.member.player.usecase

import com.monglife.mongs.domain.member.player.repository.PlayerRepository
import com.mongs.wear.core.exception.data.ExchangeStarPointException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.ExchangeStarPointUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 스타 포인트 환전 UseCase
 */
class ExchangeStarPointUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
) : BaseParamUseCase<ExchangeStarPointUseCase.Param, Unit>() {

    override suspend fun execute(param: Param) {
        withContext(Dispatchers.IO) {
            playerRepository.exchangeStarPoint(
                mongId = param.mongId,
                starPoint = param.starPoint,
            )
        }
    }

    data class Param(
        val mongId: Long,
        val starPoint: Int,
    )

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is ExchangeStarPointException -> throw ExchangeStarPointUseCaseException()

            else -> throw ExchangeStarPointUseCaseException()
        }
    }
}
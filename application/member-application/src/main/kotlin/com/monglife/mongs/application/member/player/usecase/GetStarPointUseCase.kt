package com.monglife.mongs.domain.member.player.usecase

import com.monglife.mongs.domain.member.player.repository.PlayerRepository
import com.mongs.wear.core.exception.data.GetPlayerException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.GetStarPointUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 스타 포인트 조회 UseCase
 */
class GetStarPointUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
) : BaseNoParamUseCase<Flow<Int>>() {

    override suspend fun execute(): Flow<Int> {
        return withContext(Dispatchers.IO) {
            playerRepository.updatePlayer()
            playerRepository.getStarPointLive()
        }
    }

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is GetPlayerException -> throw GetStarPointUseCaseException()

            else -> throw GetStarPointUseCaseException()
        }
    }
}
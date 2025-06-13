package com.monglife.mongs.domain.member.player.usecase

import com.monglife.mongs.domain.member.player.repository.AuthRepository
import com.monglife.mongs.domain.member.player.repository.PlayerRepository
import com.mongs.wear.core.exception.data.GetPlayerException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.UpdatePlayerUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 플레이어 정보 갱신 UseCase
 */
class UpdatePlayerUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val playerRepository: PlayerRepository,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            if (authRepository.isLogin()) {
                playerRepository.updatePlayer()
            }
        }
    }

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is GetPlayerException -> throw UpdatePlayerUseCaseException()

            else -> throw UpdatePlayerUseCaseException()
        }
    }
}
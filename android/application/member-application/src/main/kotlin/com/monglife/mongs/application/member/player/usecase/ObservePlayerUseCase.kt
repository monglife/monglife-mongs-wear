package com.monglife.mongs.application.member.player.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.application.member.player.vo.PlayerVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 슬롯 수 조회 UseCase
 */
class ObservePlayerUseCase @Inject constructor(
    private val playerWebPort: PlayerWebPort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseNoParamUseCase<Flow<PlayerVo>>() {

    @Throws(NotFoundPlayerException::class)
    override suspend fun execute(): Flow<PlayerVo> {
        return playerWebPort.getPlayer().let { response ->
            playerPersistencePort.savePlayer(player = response.toDomain())
            playerPersistencePort.getPlayerFlow()
        }.map { player ->
            player?.let {
                PlayerVo.of(player = player)
            } ?: throw NotFoundPlayerException()
        }.flowOn(Dispatchers.IO)
    }
}
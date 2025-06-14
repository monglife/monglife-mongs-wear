package com.monglife.mongs.application.member.player.usecase

import com.monglife.mongs.application.member.player.exception.InvalidCreatePlayerException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import javax.inject.Inject

/**
 * 플레이어 존재하지 않는 경우 생성 UseCase
 */
class CreatePlayerIfNotExistsUseCase @Inject constructor(
    private val playerWebPort: PlayerWebPort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    @Throws(InvalidCreatePlayerException::class)
    override suspend fun execute() {
        runCatching {
            // 플레이어 조회 요청
            playerWebPort.getPlayer().let { response ->
                // 플레이어 로컬 등록
                playerPersistencePort.savePlayer(player = response.toDomain())
            }
        }.onFailure { ex ->
            // 플레이어가 없는 경우
            if (ex is NotFoundPlayerException) {
                // 플레이어 등록 요청
                playerWebPort.createPlayer()
                // 플레이어 조회 요청
                playerWebPort.getPlayer().let { response ->
                    // 플레이어 로컬 등록
                    playerPersistencePort.savePlayer(player = response.toDomain())
                }
            }
        }
    }
}

package com.monglife.mongs.application.member.player.usecase

import com.monglife.mongs.application.member.player.exception.InvalidCreatePlayerException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 플레이어 정보 갱신 UseCase
 */
class SyncRemotePlayerUseCase @Inject constructor(
    private val playerWebPort: PlayerWebPort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    @Throws(NotFoundPlayerException::class, InvalidCreatePlayerException::class)
    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            runCatching {
                // 플레이어 조회 요청
                playerWebPort.getPlayer().let { response ->
                    // 플레이어 로컬 조회
                    playerPersistencePort.getPlayer().let { player: Player ->
                        // 플레이어 업데이트
                        player.update(
                            slotCount = response.slotCount,
                            starPoint = response.starPoint,
                        )
                        // 플레이어 로컬 등록
                        playerPersistencePort.savePlayer(player = player)
                    }
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

                throw ex
            }
        }
    }
}
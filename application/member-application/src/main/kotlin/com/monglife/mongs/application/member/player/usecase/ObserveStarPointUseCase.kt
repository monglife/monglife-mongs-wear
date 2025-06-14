package com.monglife.mongs.application.member.player.usecase

import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 스타 포인트 조회 UseCase
 */
class ObserveStarPointUseCase @Inject constructor(
    private val playerWebPort: PlayerWebPort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseNoParamUseCase<Flow<Int>>() {

    @Throws(NotFoundPlayerException::class)
    override suspend fun execute(): Flow<Int> {
        return withContext(Dispatchers.IO) {
            runCatching {
                // 플레이어 조회 요청
                playerWebPort.getPlayer().let { response ->
                    // 플레이어 로컬 등록
                    playerPersistencePort.savePlayer(player = response.toDomain())
                    // 플레이어 Flow 로컬 조회
                    playerPersistencePort.getPlayerFlow()
                }
            }.getOrElse {
                // 플레이어 Flow 로컬 조회
                playerPersistencePort.getPlayerFlow()
            }.map { player: Player ->
                // starPoint 반환o
                player.starPoint
            }
        }
    }
}
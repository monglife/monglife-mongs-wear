package com.monglife.mongs.application.member.player.usecase

import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 플레이어 정보 갱신 UseCase
 */
class SyncPlayerFromServerUseCase @Inject constructor(
    private val playerWebPort: PlayerWebPort,
    private val authPersistencePort: AuthPersistencePort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    @Throws(NotFoundPlayerException::class)
    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            // 로그인 여부 확인
            if (authPersistencePort.isExistsSession()) {
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
            }
        }
    }
}
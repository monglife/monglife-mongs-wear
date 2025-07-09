package com.monglife.mongs.application.member.player.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.member.player.exception.InvalidCreatePlayerException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.domain.member.player.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 플레이어 정보 갱신 UseCase
 */
class SyncRemotePlayerUseCase @Inject constructor(
    private val authPersistencePort: AuthPersistencePort,
    private val playerWebPort: PlayerWebPort,
    private val playerPersistencePort: PlayerPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    @Throws(InvalidCreatePlayerException::class)
    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            // 플레이어 조회 요청
            try {
                playerWebPort.getPlayer().let { response ->
                    // 플레이어 로컬 조회
                    val player = playerPersistencePort.getPlayer()?.also {
                        it.update(
                            slotCount = response.slotCount,
                            starPoint = response.starPoint,
                        )
                    } ?:let {
                        authPersistencePort.getSession()?.let {
                            playerPersistencePort.savePlayer(
                                player = Player(
                                    accountId = it.accountId,
                                    slotCount = response.slotCount,
                                    starPoint = response.starPoint,
                                )
                            )
                        } ?: throw InvalidCreatePlayerException()
                    }

                    playerPersistencePort.savePlayer(player = player)
                }
            } catch (e: NotFoundPlayerException) {
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
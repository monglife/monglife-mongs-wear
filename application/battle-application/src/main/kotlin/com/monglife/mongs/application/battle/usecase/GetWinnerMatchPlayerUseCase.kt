package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.exception.NotFoundWinnerMatchPlayerException
import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.vo.WinnerMatchPlayerVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 승리 매치 플레이어 조회 UseCase
 */
class GetWinnerMatchPlayerUseCase @Inject constructor(
    private val matchWebPort: MatchWebPort,
) : BaseParamUseCase<GetWinnerMatchPlayerUseCase.Command, WinnerMatchPlayerVo>() {

    @Throws(NotFoundWinnerMatchPlayerException::class)
    override suspend fun execute(command: Command): WinnerMatchPlayerVo {
        return withContext(Dispatchers.IO) {
            matchWebPort.getWinnerMatchPlayer(matchId = command.matchId).let { response ->
                WinnerMatchPlayerVo(
                    playerId = response.playerId,
                    mongCode = response.mongCode,
                    mongName = response.mongName,
                    name = response.name,
                    rewardPayPoint = response.rewardPayPoint,
                )
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
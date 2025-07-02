package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.NotFoundWinnerMatchPlayerException
import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.vo.WinMatchPlayerVo
import com.monglife.core.application.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 종료 UseCase
 */
class GetWinnerMatchPlayerUseCase @Inject constructor(
    private val matchWebPort: MatchWebPort,
) : BaseParamUseCase<GetWinnerMatchPlayerUseCase.Command, WinMatchPlayerVo>() {

    @Throws(NotFoundWinnerMatchPlayerException::class)
    override suspend fun execute(command: Command): WinMatchPlayerVo {
        return withContext(Dispatchers.IO) {
            // 매치 승리 플레이어 정보 조회 요청
            matchWebPort.getWinnerMatchPlayer(matchId = command.matchId).let { response ->
                // WinMatchPlayerVo 반환
                WinMatchPlayerVo(
                    playerId = response.playerId,
                    mongCode = response.mongCode,
                    mongName = response.mongName,
                    name = response.mongName,
                )
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
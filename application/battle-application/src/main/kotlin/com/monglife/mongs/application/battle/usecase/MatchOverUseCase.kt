package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.exception.NotFoundWinnerMatchPlayerException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.vo.WinMatchPlayerVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 종료 UseCase
 */
class MatchOverUseCase @Inject constructor(
    private val matchWebPort: MatchWebPort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<MatchOverUseCase.Command, WinMatchPlayerVo>() {

    @Throws(NotFoundMatchException::class, NotFoundWinnerMatchPlayerException::class)
    override suspend fun execute(command: Command): WinMatchPlayerVo {
        return withContext(Dispatchers.IO) {
            matchWebPort.getWinnerMatchPlayer(matchId = command.matchId).let {
                matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                    // 매치 종료
                    match.over()
                    // 매치 영속화
                    matchPersistencePort.saveMatch(match = match)
                }
                // WinMatchPlayerVo 반환
                WinMatchPlayerVo(
                    playerId = it.playerId,
                    mongCode = it.mongCode,
                    mongName = it.mongName,
                    name = it.mongName,
                )
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
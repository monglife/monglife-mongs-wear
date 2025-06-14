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
            // 매치 승리 플레이어 정보 조회 요청
            matchWebPort.getWinnerMatchPlayer(matchId = command.matchId).let { response ->
                // 매치 로컬 조회
                matchPersistencePort.getMatch(matchId = command.matchId).let { match: Match ->
                    // 매치 종료 변경
                    match.over()
                    // 매치 로컬 저장
                    matchPersistencePort.saveMatch(match = match)
                }
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
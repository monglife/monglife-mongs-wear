package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.NotFoundMatchPlayerException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.vo.MatchPlayerVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.MatchPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 플레이어 목록 조회 UseCase
 */
class ObserveMatchPlayersUseCase @Inject constructor(
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<ObserveMatchPlayersUseCase.Command, List<Flow<MatchPlayerVo>>>() {

    @Throws(NotFoundMatchPlayerException::class)
    override suspend fun execute(command: Command): List<Flow<MatchPlayerVo>> {
        return withContext(Dispatchers.IO) {
            // 매치 플레이어 flow 목록 조회
            matchPersistencePort.getMatchPlayersFlow(
                matchId = command.matchId,
            ).let { matchPlayerFlows: List<Flow<MatchPlayer>> ->
                // 매치가 없는 경우 예외 발생
                if (matchPlayerFlows.isEmpty()) {
                    throw NotFoundMatchPlayerException()
                }
                // 변환
                matchPlayerFlows.map { matchPlayerFlow: Flow<MatchPlayer> ->
                    matchPlayerFlow.map { matchPlayer: MatchPlayer ->
                        MatchPlayerVo.of(matchPlayer = matchPlayer)
                    }
                }
            }
        }
    }

    data class Command(
        val matchId: Long,
    )
}
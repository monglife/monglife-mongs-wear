package com.monglife.mongs.application.battle.usecase

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
 * 배틀 플레이어 라이브 객체 목록 조회 UseCase
 */
class ObserveMatchPlayersUseCase @Inject constructor(
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<ObserveMatchPlayersUseCase.Command, List<Flow<MatchPlayerVo>>>() {

    override suspend fun execute(command: Command): List<Flow<MatchPlayerVo>> {
        return withContext(Dispatchers.IO) {
            // 매치 플레이어 라이브 객체 목록 로컬 조회
            matchPersistencePort.getMatchPlayersFlow(
                matchId = command.matchId,
            ).let { matchPlayerFlows: List<Flow<MatchPlayer>> ->
                // MatchPlayer Flow 도메인 MatchPlayerVo Flow 로 변환
                matchPlayerFlows.map { matchPlayerFlow: Flow<MatchPlayer> ->
                    matchPlayerFlow.map { matchPlayer: MatchPlayer ->
                        // MatchPlayerVo 반환
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
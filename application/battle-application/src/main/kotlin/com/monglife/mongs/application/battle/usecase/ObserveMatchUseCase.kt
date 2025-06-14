package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 정보 조회 UseCase
 */
class ObserveMatchUseCase @Inject constructor(
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<ObserveMatchUseCase.Command, Flow<MatchVo>>() {

    @Throws(NotFoundMatchException::class)
    override suspend fun execute(command: Command): Flow<MatchVo> {
        return withContext(Dispatchers.IO) {
            // 매치 라이브 객체 로컬 조회
            matchPersistencePort.getMatchFlow(deviceId = command.deviceId).map { match: Match ->
                // MatchVo 반환
                MatchVo.of(match = match)
            }
        }
    }

    data class Command(
        val deviceId: String,
    )
}
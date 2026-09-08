package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.vo.MatchVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 매치 Flow 조회 UseCase
 */
class ObserveMatchUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val matchPersistencePort: MatchPersistencePort,
    ) : BaseParamUseCase<ObserveMatchUseCase.Command, Flow<MatchVo?>>() {

    override suspend fun execute(command: Command): Flow<MatchVo?> {

        val deviceId = devicePersistencePort.getDeviceId()

        return matchPersistencePort.getMatchFlow(matchId = command.matchId).map { match ->
            match?.let { MatchVo.of(match = it, deviceId = deviceId) }
        }.flowOn(Dispatchers.IO)
    }

    data class Command(
        val matchId: Long,
        val playerId: String,
    )
}
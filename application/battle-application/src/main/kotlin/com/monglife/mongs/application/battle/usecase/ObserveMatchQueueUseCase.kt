package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.battle.port.persistence.MatchQueuePersistencePort
import com.monglife.mongs.application.battle.vo.MatchQueueVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 매치 큐 Flow 조회 UseCase
 */
class ObserveMatchQueueUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val matchQueuePersistencePort: MatchQueuePersistencePort,
) : BaseParamUseCase<ObserveMatchQueueUseCase.Command, Flow<MatchQueueVo?>>() {

    override suspend fun execute(command: Command): Flow<MatchQueueVo?> {

        val deviceId = devicePersistencePort.getDeviceId()

        return matchQueuePersistencePort.createMatchQueue(mongId = command.mongId, deviceId = deviceId).map { matchQueue ->
            matchQueue?.let { MatchQueueVo.of(matchQueue = it) }
        }.flowOn(Dispatchers.IO)
    }

    data class Command(
        val mongId: Long,
    )
}
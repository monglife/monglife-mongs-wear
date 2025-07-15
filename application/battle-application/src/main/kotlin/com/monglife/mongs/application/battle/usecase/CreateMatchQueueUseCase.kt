package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.exception.InvalidCreateQueuePlayerException
import com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.battle.port.persistence.MatchQueuePersistencePort
import com.monglife.mongs.application.battle.port.web.MatchQueueWebPort
import com.monglife.mongs.application.battle.vo.MatchQueueVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 매치 큐 등록 UseCase
 */
class CreateMatchQueueUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val matchQueueWebPort: MatchQueueWebPort,
    private val matchQueuePersistencePort: MatchQueuePersistencePort,
) : BaseParamUseCase<CreateMatchQueueUseCase.Command, Flow<MatchQueueVo?>>() {

    @Throws(InvalidCreateQueuePlayerException::class)
    override suspend fun execute(command: Command): Flow<MatchQueueVo?> {

        val deviceId = devicePersistencePort.getDeviceId()

        val matchQueueFlow = matchQueuePersistencePort.createMatchQueue(mongId = command.mongId, deviceId = deviceId).map { matchQueue ->
            matchQueue?.let { MatchQueueVo.of(matchQueue = it) }
        }.flowOn(Dispatchers.IO)

        matchQueueWebPort.createQueuePlayer( mongId = command.mongId, deviceId = deviceId)

        return matchQueueFlow
    }

    data class Command(
        val mongId: Long,
    )
}
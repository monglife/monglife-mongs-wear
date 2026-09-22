package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.exception.InvalidCreateQueuePlayerException
import com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.battle.port.web.MatchQueueWebPort
import javax.inject.Inject

/**
 * 매치 큐 등록 UseCase
 */
class CreateMatchQueueUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val matchQueueWebPort: MatchQueueWebPort,
) : BaseParamUseCase<CreateMatchQueueUseCase.Command, Unit>() {

    @Throws(InvalidCreateQueuePlayerException::class)
    override suspend fun execute(command: Command) {

        val deviceId = devicePersistencePort.getDeviceId()

        matchQueueWebPort.createQueuePlayer( mongId = command.mongId, deviceId = deviceId)
    }

    data class Command(
        val mongId: Long,
    )
}
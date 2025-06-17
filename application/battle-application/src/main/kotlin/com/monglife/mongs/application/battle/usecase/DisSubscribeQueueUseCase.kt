package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeQueueException
import com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 큐 구독 해제 UseCase
 */
class DisSubscribeQueueUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val queueSubscribePort: QueueSubscribePort,
) : BaseNoParamUseCase<Unit>() {

    @Throws(InvalidDisSubscribeQueueException::class)
    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            val deviceId = devicePersistencePort.getDeviceId()
            // 매치 큐 구독 해제
            queueSubscribePort.disSubscribeQueue(deviceId = deviceId)
        }
    }
}
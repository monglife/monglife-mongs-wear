package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidSubscribeQueueException
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubscribeQueueUseCase @Inject constructor(
    private val queueSubscribePort: QueueSubscribePort,
) : BaseParamUseCase<SubscribeQueueUseCase.Command, Unit>() {

    @Throws(InvalidSubscribeQueueException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 큐 구독
            queueSubscribePort.subscribeQueue(deviceId = command.deviceId)
        }
    }

    data class Command(
        val deviceId: String,
    )
}
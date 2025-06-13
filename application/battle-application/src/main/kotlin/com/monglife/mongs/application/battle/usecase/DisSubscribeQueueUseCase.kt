package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeQueueException
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DisSubscribeQueueUseCase @Inject constructor(
    private val queueSubscribePort: QueueSubscribePort,
) : BaseParamUseCase<DisSubscribeQueueUseCase.Command, Unit>() {

    @Throws(InvalidDisSubscribeQueueException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 큐 구독 해제
            queueSubscribePort.disSubscribeQueue(deviceId = command.deviceId)
        }
    }

    data class Command(
        val deviceId: String,
    )
}
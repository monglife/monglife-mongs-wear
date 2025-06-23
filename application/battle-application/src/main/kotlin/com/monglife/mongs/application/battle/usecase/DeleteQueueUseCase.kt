package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidDeleteQueuePlayerException
import com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import com.monglife.mongs.application.battle.port.web.QueueWebPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 매칭 취소 UseCase
 */
class DeleteQueueUseCase @Inject constructor(
    private val queueWebPort: QueueWebPort,
    private val queueSubscribePort: QueueSubscribePort,
    private val devicePersistencePort: DevicePersistencePort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<DeleteQueueUseCase.Command, Unit>() {

    @Throws(InvalidDeleteQueuePlayerException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 큐 삭제 요청
            queueWebPort.deleteQueuePlayer(mongId = command.mongId)
            // 매치 큐 구독 해제
            queueSubscribePort.disSubscribeQueue(deviceId = devicePersistencePort.getDeviceId())
            // 모든 매치 삭제
            matchPersistencePort.deleteAllMatch()
        }
    }

    data class Command(
        val mongId: Long,
    )
}
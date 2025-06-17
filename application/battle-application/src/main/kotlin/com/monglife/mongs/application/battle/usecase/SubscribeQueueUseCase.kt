package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidSubscribeQueueException
import com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubscribeQueueUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val queueSubscribePort: QueueSubscribePort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    @Throws(InvalidSubscribeQueueException::class)
    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            // 매치 큐 구독
            val deviceId = devicePersistencePort.getDeviceId()
            queueSubscribePort.subscribeQueue(deviceId = deviceId).collect({ updateQueueEvent ->
                matchPersistencePort.getMatch(matchId = updateQueueEvent.matchId)
                    .let { match: Match ->
                        match.matching()
                        matchPersistencePort.saveMatch(match = match)
                    }
            })
        }
    }
}
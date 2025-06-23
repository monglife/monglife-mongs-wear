package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.exception.InvalidSubscribeMongException
import com.monglife.mongs.application.mong.port.subscribe.ManagementSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 구독 UseCase
 */
class SubscribeMongUseCase  @Inject constructor(
    private val managementSubscribePort: ManagementSubscribePort,
) : BaseParamUseCase<SubscribeMongUseCase.Command, Unit>() {

    @Throws(InvalidSubscribeMongException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 몽 구독
            managementSubscribePort.subscribeMong(mongId = command.mongId)
        }
    }

    data class Command(
        val mongId: Long,
    )
}
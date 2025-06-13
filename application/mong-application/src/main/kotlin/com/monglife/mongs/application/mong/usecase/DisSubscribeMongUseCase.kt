package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidDisSubscribeMongException
import com.monglife.mongs.application.mong.port.subscribe.ManagementSubscribePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 구독 해제 UseCase
 */
class DisSubscribeMongUseCase @Inject constructor(
    private val managementSubscribePort: ManagementSubscribePort,
) : BaseParamUseCase<DisSubscribeMongUseCase.Command, Unit>() {

    @Throws(InvalidDisSubscribeMongException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 몽 구독 해제
            managementSubscribePort.disSubscribeMong(mongId = command.mongId)
        }
    }

    data class Command(
        val mongId: Long,
    )
}
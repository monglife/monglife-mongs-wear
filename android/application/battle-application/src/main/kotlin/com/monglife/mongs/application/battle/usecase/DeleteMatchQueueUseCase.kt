package com.monglife.mongs.application.battle.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.battle.exception.InvalidDeleteQueuePlayerException
import com.monglife.mongs.application.battle.port.web.MatchQueueWebPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 큐 삭제 UseCase
 */
class DeleteMatchQueueUseCase @Inject constructor(
    private val matchQueueWebPort: MatchQueueWebPort,
) : BaseParamUseCase<DeleteMatchQueueUseCase.Command, Unit>() {

    @Throws(InvalidDeleteQueuePlayerException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 큐 삭제 요청
            matchQueueWebPort.deleteQueuePlayer(mongId = command.mongId)
        }
    }

    data class Command(
        val mongId: Long,
    )
}
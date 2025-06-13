package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidDeleteMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 삭제 UseCase
 */
class DeleteMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<DeleteMongUseCase.Command, Unit>() {

    @Throws(NotFoundMongException::class, InvalidDeleteMongException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            managementWebPort.getMong(mongId = command.mongId).let {
                // 몽 삭제 요청
                managementWebPort.deleteMong(mongId = it.mongId)
                // 몽 로컬 삭제
                managementPersistencePort.deleteMong(mongId = it.mongId)
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}
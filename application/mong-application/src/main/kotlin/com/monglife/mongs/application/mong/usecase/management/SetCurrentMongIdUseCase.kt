package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 현재 몽 ID 설정 UseCase
 */
class SetCurrentMongIdUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<SetCurrentMongIdUseCase.Command, Unit>() {

    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 현재 몽으로 설정
            managementPersistencePort.setCurrentMongId(mongId = command.mongId)
        }
    }

    data class Command(
        val mongId: Long,
    )
}
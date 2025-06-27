package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 현재 몽 ID 설정 UseCase
 */
class DeleteCurrentMongIdUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            managementPersistencePort.deleteCurrentMongId()
        }
    }
}
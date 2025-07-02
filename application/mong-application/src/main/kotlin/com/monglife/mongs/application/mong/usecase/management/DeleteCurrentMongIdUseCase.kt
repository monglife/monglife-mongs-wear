package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.core.application.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 현재 몽 ID 설정 UseCase
 */
class DeleteCurrentMongIdUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            devicePersistencePort.deleteCurrentMongId()
        }
    }
}
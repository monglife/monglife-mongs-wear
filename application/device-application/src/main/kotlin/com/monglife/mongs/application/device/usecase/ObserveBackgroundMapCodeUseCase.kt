package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 배경 코드 Flow 조회 UseCase
 */
class ObserveBackgroundMapCodeUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Flow<String?>>() {

    override suspend fun execute(): Flow<String?> {
        return devicePersistencePort.getDeviceOptionFlow()
            .map { it.backgroundMapCode }
            .flowOn(Dispatchers.IO)
    }
}
package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.core.application.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 걸음 수 Flow 조회 UseCase
 */
class ObserveCurrentWalkingCountUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Flow<Int>>() {

    override suspend fun execute(): Flow<Int> =
        // Step 현재 사용 가능한 총 걸음 수 Flow 반환
        devicePersistencePort.getStepFlow().map {
            it.getCurrentWalkingCount()
        }.flowOn(Dispatchers.IO)
}
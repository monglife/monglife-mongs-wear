package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.device.model.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 걸음 수 조회 UseCase
 */
class ObserveCurrentWalkingCountUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Flow<Int>>() {

    override suspend fun execute(): Flow<Int> {
        return withContext(Dispatchers.IO) {
            // Step 로컬 조회
            devicePersistencePort.getStepFlow().map { step: Step ->
                // Step 현재 사용 가능한 총 걸음 수 Flow 반환
                step.getCurrentWalkingCount()
            }
        }
    }
}
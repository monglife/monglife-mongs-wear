package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundStepException
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
class GetStepUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Flow<Int>>() {

    @Throws(NotFoundStepException::class)
    override suspend fun execute(): Flow<Int> {
        return withContext(Dispatchers.IO) {
            runCatching { devicePersistencePort.getStepFlow() }
                .getOrElse {
                    // 로컬 Step 등록
                    devicePersistencePort.saveStep(step = Step())
                    devicePersistencePort.getStepFlow()
                }.map { it.getWalkingCount() }
        }
    }
}
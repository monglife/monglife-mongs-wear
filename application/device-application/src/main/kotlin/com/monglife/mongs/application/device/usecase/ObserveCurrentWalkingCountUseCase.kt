package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.vo.StepVo
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
) : BaseNoParamUseCase<Flow<StepVo>>() {

    override suspend fun execute(): Flow<StepVo> {
        return devicePersistencePort.getStepFlow()
            .map { StepVo.of(it) }
            .flowOn(Dispatchers.IO)
    }
}

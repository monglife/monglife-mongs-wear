package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 걸음 수 수집 시작 UseCase
 *
 * 수집 경로를 정하고 등록한다. 멱등이라 화면에 들어올 때마다 불러도 된다.
 */
class StartStepCollectionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            devicePersistencePort.startStepCollection()
        }
    }
}

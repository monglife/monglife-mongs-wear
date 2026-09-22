package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 최초 가이드 표시 여부 조회 UseCase
 */
class GetInitGuideOpenOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Boolean>() {

    override suspend fun execute(): Boolean {
        return withContext(Dispatchers.IO) {
            devicePersistencePort.getDeviceOption().initGuideOpen
        }
    }
}
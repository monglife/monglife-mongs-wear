package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배경 코드 설정 UseCase
 */
class DeleteBackgroundMapCodeUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            devicePersistencePort.getDeviceOption().let {
                // DeviceOption backgroundMapCode 변경
                it.deleteBackgroundMapCode()

                // DeviceOption 로컬 등록
                devicePersistencePort.saveDeviceOption(deviceOption = it)
            }
        }
    }
}
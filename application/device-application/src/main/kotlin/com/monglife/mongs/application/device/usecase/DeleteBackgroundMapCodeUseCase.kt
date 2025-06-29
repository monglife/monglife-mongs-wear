package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
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
            // DeviceOption 로컬 조회
            devicePersistencePort.getDeviceOption().let { deviceOption: DeviceOption ->
                // DeviceOption backgroundMapCode 변경
                deviceOption.deleteBackgroundMapCode()
                // DeviceOption 로컬 등록
                devicePersistencePort.saveDeviceOption(deviceOption = deviceOption)
            }
        }
    }
}
package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 볼륨 조회 UseCase
 */
class GetSoundVolumeUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Float>() {

    override suspend fun execute(): Float {
        return withContext(Dispatchers.IO) {
            // DeviceOption 로컬 조회
            devicePersistencePort.getDeviceOption().soundVolume
        }
    }
}
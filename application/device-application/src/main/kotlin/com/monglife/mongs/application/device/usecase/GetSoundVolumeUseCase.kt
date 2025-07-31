package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
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
            devicePersistencePort.getDeviceOption().soundVolume
        }
    }
}
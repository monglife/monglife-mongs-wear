package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 볼륨 설정 UseCase
 */
class SetSoundVolumeUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<SetSoundVolumeUseCase.Command, Unit>() {

    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            devicePersistencePort.getDeviceOption().let {
                // DeviceOption soundVolume 변경
                it.updateSoundVolume(soundVolume = command.soundVolume)

                // DeviceOption 로컬 등록
                devicePersistencePort.saveDeviceOption(deviceOption = it)
            }
        }
    }

    data class Command(
        val soundVolume: Float
    )
}
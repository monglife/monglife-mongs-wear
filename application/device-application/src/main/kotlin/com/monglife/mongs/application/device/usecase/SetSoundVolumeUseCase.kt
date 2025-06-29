package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
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
            // DeviceOption 로컬 조회
            devicePersistencePort.getDeviceOption().let { deviceOption: DeviceOption ->
                // DeviceOption soundVolume 변경
                deviceOption.updateSoundVolume(soundVolume = command.soundVolume)
                // DeviceOption 로컬 등록
                devicePersistencePort.saveDeviceOption(deviceOption = deviceOption)
            }
        }
    }

    data class Command(
        val soundVolume: Float
    )
}
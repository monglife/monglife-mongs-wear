package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundDeviceOptionException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배경 코드 설정 UseCase
 */
class SetBackgroundMapCodeUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<SetBackgroundMapCodeUseCase.Command, Unit>() {

    @Throws(NotFoundDeviceOptionException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            runCatching { devicePersistencePort.getDeviceOption() }
                .getOrElse { DeviceOption() }
                .let { deviceOption: DeviceOption ->
                    deviceOption.updateBackgroundMapCode(backgroundMapCode = command.mapCode)
                    devicePersistencePort.saveDeviceOption(deviceOption = deviceOption)
                }
        }
    }

    data class Command(
        val mapCode: String
    )
}
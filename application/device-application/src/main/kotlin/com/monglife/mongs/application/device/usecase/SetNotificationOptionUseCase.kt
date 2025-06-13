package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundDeviceOptionException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 알림 플래그 설정 UseCase
 */
class SetNotificationOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<SetNotificationOptionUseCase.Command, Unit>() {

    @Throws(NotFoundDeviceOptionException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            runCatching { devicePersistencePort.getDeviceOption() }
                .getOrElse { DeviceOption() }
                .let { deviceOption: DeviceOption ->
                    deviceOption.updateNotificationOption(notificationOption = command.notificationOption)
                    devicePersistencePort.saveDeviceOption(deviceOption = deviceOption)
                }
        }
    }

    data class Command(
        val notificationOption: Boolean
    )
}
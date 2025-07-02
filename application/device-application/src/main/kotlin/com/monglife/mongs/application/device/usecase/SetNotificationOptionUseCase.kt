package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.core.application.usecase.BaseParamUseCase
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

    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // DeviceOption 로컬 조회
            devicePersistencePort.getDeviceOption().let { deviceOption: DeviceOption ->
                // DeviceOption notificationOption 변경
                deviceOption.updateNotificationOption(notificationOption = command.notificationOption)
                // DeviceOption 로컬 등록
                devicePersistencePort.saveDeviceOption(deviceOption = deviceOption)
            }
        }
    }

    data class Command(
        val notificationOption: Boolean
    )
}
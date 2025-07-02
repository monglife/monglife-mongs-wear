package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 초기 알림 다이얼로그 오픈 여부 설정 UseCase
 */
class SetInitNotificationDialogOpenOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<SetInitNotificationDialogOpenOptionUseCase.Command, Unit>() {

    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // DeviceOption 로컬 조회
            devicePersistencePort.getDeviceOption().let { deviceOption: DeviceOption ->
                // DeviceOption mongInteractionDialogOpen 변경
                deviceOption.updateInitNotificationDialogOpen(initNotificationDialogOpen = command.isOpen)
                // DeviceOption 로컬 등록
                devicePersistencePort.saveDeviceOption(deviceOption = deviceOption)
            }
        }
    }

    data class Command(
        val isOpen: Boolean,
    )
}
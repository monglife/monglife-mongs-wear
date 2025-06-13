package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundDeviceOptionException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 상호작용 다이얼로그 오픈 여부 설정 UseCase
 */
class SetMongInteractionDialogOpenFlagUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<SetMongInteractionDialogOpenFlagUseCase.Command, Unit>() {

    @Throws(NotFoundDeviceOptionException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            runCatching { devicePersistencePort.getDeviceOption() }
                .getOrElse { DeviceOption() }
                .let { deviceOption: DeviceOption ->
                    deviceOption.updateMongInteractionDialogOpen(mongInteractionDialogOpen = command.isOpen)
                    devicePersistencePort.saveDeviceOption(deviceOption = deviceOption)
                }
        }
    }

    data class Command(
        val isOpen: Boolean,
    )
}
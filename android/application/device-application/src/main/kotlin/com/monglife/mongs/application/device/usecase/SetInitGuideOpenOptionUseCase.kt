package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 최초 가이드 표시 여부 설정 UseCase
 */
class SetInitGuideOpenOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<SetInitGuideOpenOptionUseCase.Command, Unit>() {

    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            devicePersistencePort.getDeviceOption().let {
                // DeviceOption initGuideOpen 변경
                it.updateInitGuideOpen(initGuideOpen = command.isOpen)

                // DeviceOption 로컬 등록
                devicePersistencePort.saveDeviceOption(deviceOption = it)
            }
        }
    }

    data class Command(
        val isOpen: Boolean,
    )
}
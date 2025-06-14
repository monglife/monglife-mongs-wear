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

    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // DeviceOption 로컬 조회
            runCatching { devicePersistencePort.getDeviceOption() }
                .getOrElse { ex ->
                    // DeviceOption 이 없는 경우
                    if (ex is NotFoundDeviceOptionException) {
                        // DeviceOption 생성
                        DeviceOption()
                    } else
                        throw ex
                }
                .let { deviceOption: DeviceOption ->
                    // DeviceOption mongInteractionDialogOpen 변경
                    deviceOption.updateMongInteractionDialogOpen(mongInteractionDialogOpen = command.isOpen)
                    // DeviceOption 로컬 등록
                    devicePersistencePort.saveDeviceOption(deviceOption = deviceOption)
                }
        }
    }

    data class Command(
        val isOpen: Boolean,
    )
}
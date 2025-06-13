package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundDeviceOptionException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 상호작용 다이얼로그 오픈 여부 조회 UseCase
 */
class GetMongInteractionDialogOpenFlagUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Boolean>() {

    @Throws(NotFoundDeviceOptionException::class)
    override suspend fun execute(): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching { devicePersistencePort.getDeviceOption() }
                .getOrElse {
                    // 새로운 DeviceOption 등록
                    devicePersistencePort.saveDeviceOption(deviceOption = DeviceOption())
                }.let { deviceOption: DeviceOption ->
                    deviceOption.mongInteractionDialogOpen
                }
        }
    }
}
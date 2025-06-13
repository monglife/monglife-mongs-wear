package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundDeviceOptionException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 네트워크 플래그 조회 UseCase
 */
class GetNetworkOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Flow<Boolean>>() {

    @Throws(NotFoundDeviceOptionException::class)
    override suspend fun execute(): Flow<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching { devicePersistencePort.getDeviceOptionFlow() }
                .getOrElse {
                    // 새로운 DeviceOption 등록
                    devicePersistencePort.saveDeviceOption(deviceOption = DeviceOption())
                    // Flow 객체 조회
                    devicePersistencePort.getDeviceOptionFlow()
                }.map { deviceOption: DeviceOption ->
                    deviceOption.networkOption
                }
        }
    }
}
package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.device.model.DeviceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 알림 플래그 Flow 조회 UseCase
 */
class ObserveNotificationOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Flow<Boolean>>() {

    override suspend fun execute(): Flow<Boolean> {
        return withContext(Dispatchers.IO) {
            // DeviceOption 로컬 조회
            devicePersistencePort.getDeviceOptionFlow().map { deviceOption: DeviceOption ->
                // notificationOption Flow 반환
                deviceOption.notificationOption
            }
        }
    }
}
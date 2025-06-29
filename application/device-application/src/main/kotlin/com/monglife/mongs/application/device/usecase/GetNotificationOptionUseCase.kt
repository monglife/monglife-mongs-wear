package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 알림 플래그 조회 UseCase
 */
class GetNotificationOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Boolean>() {

    override suspend fun execute(): Boolean {
        return withContext(Dispatchers.IO) {
            // DeviceOption 로컬 조회
            devicePersistencePort.getDeviceOption().notificationOption
        }
    }
}
package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 알림 플래그 Flow 조회 UseCase
 */
class ObserveNotificationOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Flow<Boolean>>() {

    override suspend fun execute(): Flow<Boolean> {
        return devicePersistencePort.getDeviceOptionFlow()
            .map { it.notificationOption }
            .flowOn(Dispatchers.IO)
    }
}
package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 상호작용 다이얼로그 오픈 여부 조회 UseCase
 */
class GetInitNotificationDialogOpenOptionUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Boolean>() {

    override suspend fun execute(): Boolean {
        return withContext(Dispatchers.IO) {
            devicePersistencePort.getDeviceOption().initNotificationDialogOpen
        }
    }
}
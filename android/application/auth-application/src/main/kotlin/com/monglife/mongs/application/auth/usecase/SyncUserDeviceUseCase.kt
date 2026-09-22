package com.monglife.mongs.application.auth.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.auth.exception.InvalidCreateUserDeviceException
import com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.auth.port.web.UserDeviceWebPort
import com.monglife.mongs.application.auth.port.web.request.CreateDeviceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 기기 정보 서버 동기화 UseCase
 */
class SyncUserDeviceUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val userDeviceWebPort: UserDeviceWebPort,
) : BaseParamUseCase<SyncUserDeviceUseCase.Command, Unit>() {

    @Throws(InvalidCreateUserDeviceException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            val deviceId = devicePersistencePort.getDeviceId()
            val appPackageName = devicePersistencePort.getAppPackageName()
            val deviceName = devicePersistencePort.getDeviceName()

            // 기기 등록 요청
            userDeviceWebPort.createDevice(
                createDeviceRequest = CreateDeviceRequest(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appPackageName = appPackageName,
                    fcmToken = command.fcmToken,
                )
            )
        }
    }

    data class Command(
        val fcmToken: String,
    )
}
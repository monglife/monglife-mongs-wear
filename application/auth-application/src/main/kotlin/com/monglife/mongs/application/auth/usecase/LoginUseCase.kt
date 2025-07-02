package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.exception.InvalidCreateUserDeviceException
import com.monglife.mongs.application.auth.exception.InvalidLoginException
import com.monglife.mongs.application.auth.exception.NeedJoinException
import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.application.auth.port.web.UserDeviceWebPort
import com.monglife.mongs.application.auth.port.web.request.CreateDeviceRequest
import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 로그인 UseCase
 */
class LoginUseCase @Inject constructor(
    private val authWebPort: AuthWebPort,
    private val devicePersistencePort: DevicePersistencePort,
    private val userDeviceWebPort: UserDeviceWebPort,
    private val authPersistencePort: AuthPersistencePort,
) : BaseParamUseCase<LoginUseCase.Command, Unit>() {

    @Throws(InvalidLoginException::class, NeedJoinException::class, InvalidCreateUserDeviceException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            val deviceId = devicePersistencePort.getDeviceId()
            val appPackageName = devicePersistencePort.getAppPackageName()
            val buildVersion = devicePersistencePort.getBuildVersion()
            val deviceName = devicePersistencePort.getDeviceName()
            val fcmToken = devicePersistencePort.getFcmToken()

            // 기기 등록 요청
            userDeviceWebPort.createDevice(
                createDeviceRequest = CreateDeviceRequest(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appPackageName = appPackageName,
                    fcmToken = fcmToken,
                )
            )

            // 로그인 요청
            authWebPort.login(
                email = command.email,
                googleAccountId = command.googleAccountId,
                deviceId = deviceId,
                appPackageName = appPackageName,
                deviceName = deviceName,
                buildVersion = buildVersion,
            ).let { response ->
                // 세션 로컬 등록
                authPersistencePort.saveSession(
                    session = Session(
                        accountId = response.accountId,
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        version = 1L,
                    )
                )
            }
        }
    }

    data class Command(
        val googleAccountId: String,
        val email: String,
    )
}
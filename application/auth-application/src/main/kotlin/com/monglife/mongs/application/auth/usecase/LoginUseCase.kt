package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.exception.InvalidCreateUserDeviceException
import com.monglife.mongs.application.auth.exception.InvalidLoginException
import com.monglife.mongs.application.auth.exception.NeedJoinException
import com.monglife.mongs.application.auth.exception.NeedUpdateAppException
import com.monglife.mongs.application.auth.exception.VerifyAppVersionException
import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.application.auth.port.web.UserDeviceWebPort
import com.monglife.mongs.application.auth.port.web.request.CreateDeviceRequest
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 로그인 UseCase
 */
class LoginUseCase @Inject constructor(
    private val authWebPort: AuthWebPort,
    private val userDeviceWebPort: UserDeviceWebPort,
    private val authPersistencePort: AuthPersistencePort,
) : BaseParamUseCase<LoginUseCase.Command, Unit>() {

    @Throws(VerifyAppVersionException::class, NeedUpdateAppException::class, InvalidLoginException::class, NeedJoinException::class, InvalidCreateUserDeviceException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 앱 버전 검증 조회 요청
            authWebPort.verifyAppVersion(
                appPackageName = command.appPackageName,
                buildVersion = command.buildVersion,
            ).let { response ->
                if (response.mustUpdate) {
                    // 앱 업데이트 필요한 경우 예외 발생
                    throw NeedUpdateAppException()
                }
            }

            // 로그인 요청
            authWebPort.login(
                deviceId = command.deviceId,
                email = command.email,
                googleAccountId = command.googleAccountId,
                appPackageName = command.appPackageName,
                deviceName = command.deviceName,
                buildVersion = command.buildVersion,
            ).let { response ->
                // 세션 로컬 등록
                authPersistencePort.saveSession(session = Session(
                    accountId = response.accountId,
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                ))

                // 기기 등록 요청
                userDeviceWebPort.createDevice(
                    createDeviceRequest = CreateDeviceRequest(
                        deviceId = command.deviceId,
                        deviceName = command.deviceName,
                        appPackageName = command.appPackageName,
                        fcmToken = command.fcmToken,
                    )
                )
            }
        }
    }

    data class Command(
        val googleAccountId: String,
        val email: String,
        val deviceId: String,
        val deviceName: String,
        val appPackageName: String,
        val buildVersion: String,
        val fcmToken: String,
    )
}
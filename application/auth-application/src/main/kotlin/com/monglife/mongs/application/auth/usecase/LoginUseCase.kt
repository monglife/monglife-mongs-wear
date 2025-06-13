package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.exception.InvalidCreatePlayerException
import com.monglife.mongs.application.auth.exception.InvalidCreateUserDeviceException
import com.monglife.mongs.application.auth.exception.LoginException
import com.monglife.mongs.application.auth.exception.NeedJoinException
import com.monglife.mongs.application.auth.exception.NeedUpdateAppException
import com.monglife.mongs.application.auth.exception.NotFoundPlayerException
import com.monglife.mongs.application.auth.exception.VerifyAppVersionException
import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.auth.port.persistence.MongPersistencePort
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.application.auth.port.web.ManagementWebPort
import com.monglife.mongs.application.auth.port.web.PlayerWebPort
import com.monglife.mongs.application.auth.port.web.UserDeviceWebPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.auth.model.Session
import com.monglife.mongs.domain.auth.model.UserDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 로그인 UseCase
 */
class LoginUseCase @Inject constructor(
    private val authWebPort: AuthWebPort,
    private val userDeviceWebPort: UserDeviceWebPort,
    private val managementWebPort: ManagementWebPort,
    private val playerWebPort: PlayerWebPort,
    private val authPersistencePort: AuthPersistencePort,
    private val mongPersistencePort: MongPersistencePort,
) : BaseParamUseCase<LoginUseCase.Command, Unit>() {

    @Throws(
        LoginException::class,
        NeedJoinException::class,
        VerifyAppVersionException::class,
        NotFoundPlayerException::class,
        InvalidCreatePlayerException::class,
        InvalidCreateUserDeviceException::class
    )
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 앱 버전 검증
            authWebPort.verifyAppVersion(
                appPackageName = command.appPackageName,
                buildVersion = command.buildVersion,
            ).let { response ->
                if (response.mustUpdate) {
                    throw NeedUpdateAppException()
                }
            }

            // 로그인
            authWebPort.login(
                deviceId = command.deviceId,
                email = command.email,
                googleAccountId = command.googleAccountId,
                appPackageName = command.appPackageName,
                deviceName = command.deviceName,
                buildVersion = command.buildVersion,
            ).let { response ->
                // 세션 등록
                authPersistencePort.saveSession(session = Session(
                    accountId = response.accountId,
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                ))

                // 기기 등록
                userDeviceWebPort.createDevice(
                    userDevice = UserDevice(
                        deviceId = command.deviceId,
                        deviceName = command.deviceName,
                        appPackageName = command.appPackageName,
                        fcmToken = command.fcmToken,
                    )
                )

                // 플레이어 등록 조회
                runCatching {
                    playerWebPort.getPlayer(accountId = response.accountId)
                }.onFailure { ex ->
                    if (ex is NotFoundPlayerException) {
                        // 플레이어 등록
                        playerWebPort.createPlayer()
                    }
                }

                // 몽 목록 동기화
                managementWebPort.getMongs(accountId = response.accountId).forEach {
                    mongPersistencePort.saveMong(mong = it.toDomain())
                }
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
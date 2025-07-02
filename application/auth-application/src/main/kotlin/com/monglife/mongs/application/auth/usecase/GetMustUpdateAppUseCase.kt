package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.core.application.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 앱 버전 체크 UseCase
 */
class GetMustUpdateAppUseCase @Inject constructor(
    private val authWebPort: AuthWebPort,
    private val devicePersistencePort: DevicePersistencePort,
    private val authPersistencePort: AuthPersistencePort,
) : BaseNoParamUseCase<Boolean>() {

    override suspend fun execute(): Boolean {
        return withContext(Dispatchers.IO) {
            val appPackageName = devicePersistencePort.getAppPackageName()
            val buildVersion = devicePersistencePort.getBuildVersion()

            // 앱 버전 검증 조회 요청
            authWebPort.verifyAppVersion(
                appPackageName = appPackageName,
                buildVersion = buildVersion,
            ).let { response ->
                if (response.mustUpdate) {
                    // 앱 업데이트 필요한 경우
                    authPersistencePort.getSession()?.let {
                        // 세션 로컬 삭제
                        authPersistencePort.deleteSession()
                    }
                }

                response.mustUpdate
            }
        }
    }
}
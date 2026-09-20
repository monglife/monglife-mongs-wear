package com.monglife.mongs.application.auth.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.auth.exception.VerifyAppVersionException
import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 앱 진입 가능 여부.
 *
 * 강제 업데이트와 서버 점검이 서버의 같은 응답(`/public/auth/verify/version`)에 실려 오므로
 * 둘을 한 번에 판정한다. 점검용 UseCase 를 따로 두면 앱을 켤 때마다 같은 요청을 두 번 보내게 된다.
 *
 * **업데이트가 점검보다 앞선다.** 업데이트가 필요하면 세션을 지우고 스토어로 보내야 하는데,
 * 점검 화면을 먼저 띄우면 점검이 끝날 때까지 그 처리가 미뤄진다.
 */
class GetAppEntryStateUseCase @Inject constructor(
    private val authWebPort: AuthWebPort,
    private val devicePersistencePort: DevicePersistencePort,
    private val authPersistencePort: AuthPersistencePort,
) : BaseNoParamUseCase<AppEntryState>() {

    @Throws(VerifyAppVersionException::class)
    override suspend fun execute(): AppEntryState {
        return withContext(Dispatchers.IO) {
            val appPackageName = devicePersistencePort.getAppPackageName()
            val buildVersion = devicePersistencePort.getBuildVersion()

            // 앱 버전 검증 조회 요청 (서버 점검 여부가 같이 온다)
            authWebPort.verifyAppVersion(
                appPackageName = appPackageName,
                buildVersion = buildVersion,
            ).let { response ->
                when {
                    response.mustUpdate -> {
                        // 앱 업데이트 필요한 경우
                        authPersistencePort.getSession()?.let {
                            // 세션 로컬 삭제
                            authPersistencePort.deleteSession()
                        }

                        AppEntryState.NeedUpdate
                    }

                    response.underMaintenance -> AppEntryState.Maintenance(
                        message = response.maintenanceMessage,
                        endAt = response.maintenanceEndAt,
                    )

                    else -> AppEntryState.Normal
                }
            }
        }
    }
}

/**
 * 앱 진입 판정 결과.
 *
 * 점검은 세션을 건드리지 않는다. 서버가 로그인을 막지 않으므로 점검이 끝나면 그대로 이어서 쓴다.
 */
sealed class AppEntryState {

    /** 그대로 들어간다 */
    data object Normal : AppEntryState()

    /** 강제 업데이트. 세션은 이미 지워졌다 */
    data object NeedUpdate : AppEntryState()

    /**
     * 서버 점검 중.
     * @param message 서버가 준 안내 문구. 없으면 앱이 기본 문구를 쓴다
     * @param endAt '2026-09-21T04:00:00' 꼴. null 이면 종료 미정
     */
    data class Maintenance(val message: String?, val endAt: String?) : AppEntryState()
}

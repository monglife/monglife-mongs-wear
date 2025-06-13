package com.monglife.mongs.application.auth.port.web

import com.monglife.mongs.application.auth.exception.JoinException
import com.monglife.mongs.application.auth.exception.LoginException
import com.monglife.mongs.application.auth.exception.LogoutException
import com.monglife.mongs.application.auth.exception.NeedJoinException
import com.monglife.mongs.application.auth.exception.VerifyAppVersionException

interface AuthWebPort {

    /**
     * 앱 버전 검증
     */
    @Throws(VerifyAppVersionException::class)
    suspend fun verifyAppVersion(appPackageName: String, buildVersion: String): VerifyAppVersionResponse

    /**
     * 회원 가입
     */
    @Throws(JoinException::class)
    suspend fun join(email: String, name: String, googleAccountId: String)

    /**
     * 로그인
     */
    @Throws(LoginException::class, NeedJoinException::class, VerifyAppVersionException::class)
    suspend fun login(deviceId: String, email: String, googleAccountId: String, appPackageName: String, deviceName: String, buildVersion: String): LoginResponse

    /**
     * 로그 아웃
     */
    @Throws(LogoutException::class)
    suspend fun logout()

}
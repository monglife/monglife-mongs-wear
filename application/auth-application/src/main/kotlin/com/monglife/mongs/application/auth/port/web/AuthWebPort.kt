package com.monglife.mongs.application.auth.port.web

import com.monglife.mongs.application.auth.exception.InvalidJoinException
import com.monglife.mongs.application.auth.exception.InvalidLoginException
import com.monglife.mongs.application.auth.exception.InvalidLogoutException
import com.monglife.mongs.application.auth.exception.NeedJoinException
import com.monglife.mongs.application.auth.exception.VerifyAppVersionException
import com.monglife.mongs.application.auth.port.web.response.LoginResponse
import com.monglife.mongs.application.auth.port.web.response.VerifyAppVersionResponse

interface AuthWebPort {

    /**
     * 앱 버전 검증
     */
    @Throws(VerifyAppVersionException::class)
    suspend fun verifyAppVersion(appPackageName: String, buildVersion: String): VerifyAppVersionResponse

    /**
     * 회원 가입
     */
    @Throws(InvalidJoinException::class)
    suspend fun join(email: String, name: String, socialAccountId: String)

    /**
     * 로그인
     */
    @Throws(InvalidLoginException::class, NeedJoinException::class)
    suspend fun login(deviceId: String, email: String, googleAccountId: String, appPackageName: String, deviceName: String, buildVersion: String): LoginResponse

    /**
     * 로그 아웃
     */
    @Throws(InvalidLogoutException::class)
    suspend fun logout(refreshToken: String)

}
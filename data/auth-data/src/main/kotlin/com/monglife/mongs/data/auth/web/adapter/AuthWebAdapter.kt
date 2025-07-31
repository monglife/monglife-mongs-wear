package com.monglife.mongs.data.auth.web.adapter

import com.monglife.core.data.web.client.AuthWebClient
import com.monglife.core.data.web.client.request.JoinRequestDto
import com.monglife.core.data.web.client.request.LoginRequestDto
import com.monglife.core.data.web.client.request.LogoutRequestDto
import com.monglife.core.data.web.utils.HttpUtil.getErrorResponseDto
import com.monglife.mongs.application.auth.exception.InvalidJoinException
import com.monglife.mongs.application.auth.exception.InvalidLoginException
import com.monglife.mongs.application.auth.exception.InvalidLogoutException
import com.monglife.mongs.application.auth.exception.NeedJoinException
import com.monglife.mongs.application.auth.exception.VerifyAppVersionException
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.application.auth.port.web.response.LoginResponse
import com.monglife.mongs.application.auth.port.web.response.VerifyAppVersionResponse
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthWebAdapter @Inject constructor(
    private val authWebClient: AuthWebClient,
): AuthWebPort {

    companion object {
        private const val NEED_JOIN_RESPONSE_CODE = "DISCOVERY-ACCOUNT-101"
    }

    /**
     * 앱 버전 검증
     */
    @Throws(VerifyAppVersionException::class)
    override suspend fun verifyAppVersion(
        appPackageName: String,
        buildVersion: String
    ): VerifyAppVersionResponse = authWebClient.verifyAppVersion(
        appPackageName = appPackageName,
        buildVersion = buildVersion
    ).let { response ->

        val body = response.takeIf { it.isSuccessful }?.body() ?: throw VerifyAppVersionException()

        VerifyAppVersionResponse(
            appPackageName = body.result.appPackageName,
            buildVersion = body.result.buildVersion,
            mustUpdate = body.result.mustUpdate
        )
    }

    /**
     * 회원 가입
     */
    @Throws(InvalidJoinException::class)
    override suspend fun join(email: String, name: String, socialAccountId: String): Unit =
        authWebClient.join(
            joinRequestDto = JoinRequestDto(
                email = email,
                name = name,
                socialAccountId = socialAccountId,
            )
        ).let { response ->
            response.takeIf { it.isSuccessful }?.body() ?: throw InvalidJoinException()
        }

    /**
     * 로그인
     */
    @Throws(InvalidLoginException::class, NeedJoinException::class)
    override suspend fun login(
        deviceId: String,
        email: String,
        googleAccountId: String,
        appPackageName: String,
        deviceName: String,
        buildVersion: String
    ): LoginResponse = authWebClient.login(
        loginRequestDto = LoginRequestDto(
            deviceId = deviceId,
            email = email,
            socialAccountId = googleAccountId,
            appPackageName = appPackageName,
            deviceName = deviceName,
            buildVersion = buildVersion,
        )
    ).let { response ->

        val body = response.takeIf { it.isSuccessful }?.body() ?: run {
            val errorBody = response.getErrorResponseDto()

            if (errorBody.code == NEED_JOIN_RESPONSE_CODE) {
                throw NeedJoinException()
            } else {
                throw InvalidLoginException()
            }
        }

        LoginResponse(
            accountId = body.result.accountId,
            accessToken = body.result.accessToken,
            refreshToken = body.result.refreshToken
        )
    }

    /**
     * 로그아웃
     */
    @Throws(InvalidLogoutException::class)
    override suspend fun logout(refreshToken: String): Unit = authWebClient.logout(
        logoutRequestDto = LogoutRequestDto(
            refreshToken = refreshToken,
        )
    ).let { response ->
        response.takeIf { it.isSuccessful }?.body() ?: throw InvalidLogoutException()
    }
}
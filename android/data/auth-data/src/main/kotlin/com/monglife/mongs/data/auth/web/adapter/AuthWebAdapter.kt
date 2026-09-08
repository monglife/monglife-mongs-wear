package com.monglife.mongs.data.auth.web.adapter

import com.monglife.core.data.web.client.AuthApiVariant
import com.monglife.core.data.web.client.AuthWebClient
import com.monglife.core.data.web.client.request.CredentialJoinRequestDto
import com.monglife.core.data.web.client.request.CredentialLoginRequestDto
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

/**
 * 인증 웹 어댑터
 *
 * 앱이 쓰는 계약([AuthApiVariant])에 따라 호출할 엔드포인트가 갈린다.
 * 기존 엔드포인트는 idToken 을 모르는 필드로 보고 거부하므로 같은 요청에 실어 보낼 수 없다.
 */
@Singleton
class AuthWebAdapter @Inject constructor(
    private val authWebClient: AuthWebClient,
    private val authApiVariant: AuthApiVariant,
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
    override suspend fun join(email: String, name: String, socialAccountId: String, idToken: String): Unit =
        when (authApiVariant) {
            AuthApiVariant.LEGACY -> authWebClient.join(
                joinRequestDto = JoinRequestDto(
                    email = email,
                    name = name,
                    socialAccountId = socialAccountId,
                )
            )
            AuthApiVariant.CREDENTIAL -> authWebClient.credentialJoin(
                credentialJoinRequestDto = CredentialJoinRequestDto(
                    email = email,
                    name = name,
                    socialAccountId = socialAccountId,
                    idToken = idToken,
                )
            )
        }.let { response ->
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
        idToken: String,
        appPackageName: String,
        deviceName: String,
        buildVersion: String
    ): LoginResponse = when (authApiVariant) {
        AuthApiVariant.LEGACY -> authWebClient.login(
            loginRequestDto = LoginRequestDto(
                deviceId = deviceId,
                email = email,
                socialAccountId = googleAccountId,
                appPackageName = appPackageName,
                deviceName = deviceName,
                buildVersion = buildVersion,
            )
        )
        AuthApiVariant.CREDENTIAL -> authWebClient.credentialLogin(
            credentialLoginRequestDto = CredentialLoginRequestDto(
                deviceId = deviceId,
                email = email,
                socialAccountId = googleAccountId,
                idToken = idToken,
                appPackageName = appPackageName,
                deviceName = deviceName,
                buildVersion = buildVersion,
            )
        )
    }.let { response ->

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
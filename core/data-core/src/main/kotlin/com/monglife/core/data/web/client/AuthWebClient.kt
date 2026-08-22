package com.monglife.core.data.web.client

import com.monglife.core.data.web.client.request.CredentialJoinRequestDto
import com.monglife.core.data.web.client.request.CredentialLoginRequestDto
import com.monglife.core.data.web.client.request.JoinRequestDto
import com.monglife.core.data.web.client.request.LoginRequestDto
import com.monglife.core.data.web.client.request.LogoutRequestDto
import com.monglife.core.data.web.client.request.ReissueRequestDto
import com.monglife.core.data.web.client.response.LoginResponseDto
import com.monglife.core.data.web.client.response.ReissueResponseDto
import com.monglife.core.data.web.client.response.VerifyAppVersionResponseDto
import com.monglife.core.data.web.dto.response.ResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthWebClient {

    companion object {
        /**
         * Credential 계약 엔드포인트
         *
         * 아직 서버에 없다. 백엔드(monglife-mongs)에 추가할 때 실제 경로에 맞춰 이 두 상수만 고치면 된다.
         */
        const val CREDENTIAL_JOIN_PATH = "public/auth/join/credential"
        const val CREDENTIAL_LOGIN_PATH = "public/auth/login/credential"
    }
    
    /**
     * 앱 버전 검증 API 요청
     */
    @GET("public/auth/verify/version")
    suspend fun verifyAppVersion(
        @Query("appPackageName") appPackageName: String, @Query("buildVersion") buildVersion: String
    ): Response<ResponseDto<VerifyAppVersionResponseDto>>

    /**
     * 회원 가입 API 요청
     */
    @POST("public/auth/join")
    suspend fun join(@Body joinRequestDto: JoinRequestDto): Response<ResponseDto<Void>>

    /**
     * 로그인 API 요청
     */
    @POST("public/auth/login")
    suspend fun login(@Body loginRequestDto: LoginRequestDto): Response<ResponseDto<LoginResponseDto>>

    /**
     * 회원 가입 API 요청 (Credential 계약)
     *
     * idToken 을 함께 받는 별도 엔드포인트다. 기존 엔드포인트는 이 필드를 모르는 필드로 보고
     * 400/500 을 돌려주므로 계약을 나눴다.
     */
    @POST(AuthWebClient.CREDENTIAL_JOIN_PATH)
    suspend fun credentialJoin(@Body credentialJoinRequestDto: CredentialJoinRequestDto): Response<ResponseDto<Void>>

    /**
     * 로그인 API 요청 (Credential 계약)
     */
    @POST(AuthWebClient.CREDENTIAL_LOGIN_PATH)
    suspend fun credentialLogin(@Body credentialLoginRequestDto: CredentialLoginRequestDto): Response<ResponseDto<LoginResponseDto>>

    /**
     * 로그아웃 API 요청
     */
    @POST("public/auth/logout")
    suspend fun logout(@Body logoutRequestDto: LogoutRequestDto): Response<ResponseDto<Void>>

    /**
     * 토큰 재발행 API 요청
     */
    @POST("public/auth/reissue")
    suspend fun reissue(@Body reissueRequestDto: ReissueRequestDto): Response<ResponseDto<ReissueResponseDto>>
}
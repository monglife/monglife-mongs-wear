package com.monglife.mongs.data.core.client

import com.monglife.mongs.data.core.client.request.JoinRequestDto
import com.monglife.mongs.data.core.client.request.LoginRequestDto
import com.monglife.mongs.data.core.client.request.LogoutRequestDto
import com.monglife.mongs.data.core.client.request.ReissueRequestDto
import com.monglife.mongs.data.core.client.response.LoginResponseDto
import com.monglife.mongs.data.core.client.response.ReissueResponseDto
import com.monglife.mongs.data.core.client.response.VerifyAppVersionResponseDto
import com.monglife.mongs.data.core.dto.response.ResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthWebClient {
    
    /**
     * 앱 버전 검증 API 요청
     */
    @GET("auth/verify/version")
    suspend fun verifyAppVersion(
        @Query("appPackageName") appPackageName: String, @Query("buildVersion") buildVersion: String
    ): Response<ResponseDto<VerifyAppVersionResponseDto>>

    /**
     * 회원 가입 API 요청
     */
    @POST("auth/join")
    suspend fun join(@Body joinRequestDto: JoinRequestDto): Response<ResponseDto<Void>>

    /**
     * 로그인 API 요청
     */
    @POST("auth/login")
    suspend fun login(@Body loginRequestDto: LoginRequestDto): Response<ResponseDto<LoginResponseDto>>

    /**
     * 로그아웃 API 요청
     */
    @POST("auth/logout")
    suspend fun logout(@Body logoutRequestDto: LogoutRequestDto): Response<ResponseDto<Void>>

    /**
     * 토큰 재발행 API 요청
     */
    @POST("auth/reissue")
    suspend fun reissue(@Body reissueRequestDto: ReissueRequestDto): Response<ResponseDto<ReissueResponseDto>>
}
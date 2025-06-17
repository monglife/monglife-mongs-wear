package com.monglife.mongs.data.core.retrofit.client

import com.monglife.mongs.data.core.common.dto.response.ResponseDto
import com.monglife.mongs.data.core.retrofit.client.request.ReissueRequestDto
import com.monglife.mongs.data.core.retrofit.client.response.ReissueResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthWebClient {

    /**
     * 토큰 재발행 API 요청
     */
    @POST("auth/login")
    suspend fun reissue(@Body reissueRequestDto: ReissueRequestDto): Response<ResponseDto<ReissueResponseDto>>
}
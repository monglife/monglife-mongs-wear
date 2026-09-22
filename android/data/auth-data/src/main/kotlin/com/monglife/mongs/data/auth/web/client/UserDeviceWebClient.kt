package com.monglife.mongs.data.auth.web.client

import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.data.auth.web.client.request.CreateDeviceRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserDeviceWebClient {

    /**
     * 기기 등록
     */
    @POST("public/userDevice")
    suspend fun createDevice(@Body createDeviceRequestDto: CreateDeviceRequestDto) : Response<ResponseDto<Void>>
}
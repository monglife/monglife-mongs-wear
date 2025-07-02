package com.monglife.mongs.data.auth.web.client

import com.monglife.mongs.data.auth.web.client.request.CreateDeviceRequestDto
import com.monglife.core.data.web.dto.response.ResponseDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Named
import javax.inject.Singleton

interface UserDeviceWebClient {

    /**
     * 기기 등록
     */
    @POST("userDevice")
    suspend fun createDevice(@Body createDeviceRequestDto: CreateDeviceRequestDto) : Response<ResponseDto<Void>>
}
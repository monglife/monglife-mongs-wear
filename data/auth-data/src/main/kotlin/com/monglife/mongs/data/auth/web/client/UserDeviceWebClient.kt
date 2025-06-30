package com.monglife.mongs.data.auth.web.client

import com.monglife.mongs.data.auth.web.client.request.CreateDeviceRequestDto
import com.monglife.mongs.data.core.web.dto.response.ResponseDto
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

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {
    @Provides
    @Singleton
    fun provideUserDeviceWebClient(
        @Named("monglife-discovery") retrofit: Retrofit
    ): UserDeviceWebClient = retrofit.create(UserDeviceWebClient::class.java)
}

interface UserDeviceWebClient {

    /**
     * 기기 등록
     */
    @POST("userDevice")
    suspend fun createDevice(@Body createDeviceRequestDto: CreateDeviceRequestDto) : Response<ResponseDto<Void>>
}
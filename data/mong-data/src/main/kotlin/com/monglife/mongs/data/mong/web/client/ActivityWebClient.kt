package com.monglife.mongs.data.mong.web.client

import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import com.monglife.mongs.data.mong.web.client.request.TrainingEndRequestDto
import com.monglife.mongs.data.mong.web.client.response.GetTrainingResponseDto
import com.monglife.mongs.data.mong.web.client.response.TrainingEndResponseDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ActivityWebClientModule {
    @Provides
    @Singleton
    fun provideActivityWebClient(@Named("monglife-mongs") retrofit: Retrofit): ActivityWebClient =
        retrofit.create(ActivityWebClient::class.java)
}

interface ActivityWebClient {

    /**
     * 훈련 목록 조회 API 요청
     */
    @GET("character/activity/training")
    suspend fun getTrainingTypes(): Response<ResponseDto<List<GetTrainingResponseDto>>>

    /**
     * 훈련 조회 API 요청
     */
    @GET("character/activity/training/{trainingCode}")
    suspend fun getTrainingType(@Path("trainingCode") trainingCode: String): Response<ResponseDto<GetTrainingResponseDto>>

    /**
     * 훈련 완료 API 요청
     */
    @POST("character/activity/training")
    suspend fun trainingEnd(@Body trainingEndRequestDto: TrainingEndRequestDto): Response<ResponseDto<TrainingEndResponseDto>>
}
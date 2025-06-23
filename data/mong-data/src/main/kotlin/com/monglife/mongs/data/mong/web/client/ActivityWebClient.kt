package com.monglife.mongs.data.mong.web.client

import com.monglife.mongs.data.core.dto.response.ResponseDto
import com.monglife.mongs.data.mong.web.client.request.TrainingEndRequestDto
import com.monglife.mongs.data.mong.web.client.response.GetTrainingResponseDto
import com.monglife.mongs.data.mong.web.client.response.TrainingEndResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ActivityWebClient {

    /**
     * 훈련 목록 조회 API 요청
     */
    @GET("activity/training")
    suspend fun getTrainingTypes(): Response<ResponseDto<List<GetTrainingResponseDto>>>

    /**
     * 훈련 조회 API 요청
     */
    @GET("activity/training/{trainingCode}")
    suspend fun getTrainingType(@Path("trainingCode") trainingCode: String): Response<ResponseDto<GetTrainingResponseDto>>

    /**
     * 훈련 완료 API 요청
     */
    @POST("activity/training")
    suspend fun trainingEnd(@Body trainingEndRequestDto: TrainingEndRequestDto): Response<ResponseDto<TrainingEndResponseDto>>
}
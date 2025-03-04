package com.mongs.wear.data.activity.api

import com.mongs.wear.core.dto.response.ResponseDto
import com.mongs.wear.core.enums.TrainingCode
import com.mongs.wear.data.activity.dto.request.TrainingBasketballRequestDto
import com.mongs.wear.data.activity.dto.request.TrainingRunnerRequestDto
import com.mongs.wear.data.activity.dto.response.GetTrainingRunnerResponseDto
import com.mongs.wear.data.activity.dto.response.TrainingBasketballEndResponseDto
import com.mongs.wear.data.activity.dto.response.TrainingRunnerEndResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TrainingApi {

    /**
     * 훈련 정보 조회
     */
    @GET("activity/training")
    suspend fun getTrainingRunner(@Query("trainingCode") trainingCode: TrainingCode) : Response<ResponseDto<GetTrainingRunnerResponseDto>>

    /**
     * 훈련 runner 완료
     */
    @POST("activity/training/runner")
    suspend fun trainingRunner(@Body trainingRunnerRequestDto: TrainingRunnerRequestDto) : Response<ResponseDto<TrainingRunnerEndResponseDto>>

    /**
     * 훈련 basketball 완료
     */
    @POST("activity/training/basketball")
    suspend fun trainingBasketball(@Body trainingBasketballRequestDto: TrainingBasketballRequestDto) : Response<ResponseDto<TrainingBasketballEndResponseDto>>
}
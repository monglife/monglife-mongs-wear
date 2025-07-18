package com.monglife.mongs.data.device.web.client

import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.data.device.web.client.request.ExchangeCurrentWalkingCountRequestDto
import com.monglife.mongs.data.device.web.client.request.UpdateTotalWalkingCountRequestDto
import com.monglife.mongs.data.device.web.client.response.ExchangeCurrentWalkingCountResponseDto
import com.monglife.mongs.data.device.web.client.response.GetStepResponseDto
import com.monglife.mongs.data.device.web.client.response.UpdateTotalWalkingCountResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface DeviceWebClient {

    /**
     * 걸음 수 조회 API 요청
     */
    @GET("user/step")
    suspend fun getStep(): Response<ResponseDto<GetStepResponseDto>>

    /**
     * 걸음 수 환전 API 요청
     */
    @POST("user/step/exchange")
    suspend fun exchangeCurrentWalkingCount(@Body exchangeCurrentWalkingCountRequestDto: ExchangeCurrentWalkingCountRequestDto): Response<ResponseDto<ExchangeCurrentWalkingCountResponseDto>>

    /**
     * 걸음 수 동기화 API 요청
     */
    @PATCH("user/step")
    suspend fun updateTotalWalkingCount(@Body updateTotalWalkingCountRequestDto: UpdateTotalWalkingCountRequestDto): Response<ResponseDto<UpdateTotalWalkingCountResponseDto>>
}
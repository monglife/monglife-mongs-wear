package com.monglife.mongs.data.member.player.web.client

import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.player.web.client.request.ExchangeStartPointRequestDto
import com.monglife.mongs.data.member.player.web.client.response.BuySlotResponseDto
import com.monglife.mongs.data.member.player.web.client.response.ExchangeStarPointResponseDto
import com.monglife.mongs.data.member.player.web.client.response.GetPlayerResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface PlayerWebClient {

    /**
     * 플레이어 등록 API 요청
     */
    @POST("player")
    suspend fun createPlayer(): Response<ResponseDto<Void>>

    /**
     * 플레이어 조회 API 요청
     */
    @GET("player")
    suspend fun getPlayer(): Response<ResponseDto<GetPlayerResponseDto>>

    /**
     * 슬롯 구매 API 요청
     */
    @PATCH("player/slot")
    suspend fun buySlot(): Response<ResponseDto<BuySlotResponseDto>>

    /**
     * 스타 포인트 환전 API 요청
     */
    @POST("player/exchange/starPoint")
    suspend fun exchangeStarPoint(@Body exchangeStartPointRequestDto: ExchangeStartPointRequestDto): Response<ResponseDto<ExchangeStarPointResponseDto>>
}
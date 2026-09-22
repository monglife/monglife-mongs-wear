package com.monglife.mongs.data.device.web.client

import com.monglife.mongs.data.device.web.client.request.ExchangeCurrentWalkingCountRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceWebClient {

    /**
     * 걸음 수 환전 API 요청
     *
     * 응답 본문은 쓰지 않는다. 지급된 payPoint 는 몽 정보를 통해 따로 내려오고,
     * 걸음 수 잔액은 이제 기기 로컬에만 있으므로 서버가 돌려줄 것이 없다.
     */
    @POST("user/step/exchange")
    suspend fun exchangeCurrentWalkingCount(@Body exchangeCurrentWalkingCountRequestDto: ExchangeCurrentWalkingCountRequestDto): Response<Void>
}

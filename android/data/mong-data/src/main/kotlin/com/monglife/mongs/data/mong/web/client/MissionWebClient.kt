package com.monglife.mongs.data.mong.web.client

import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.data.mong.web.client.request.ClaimMissionRewardRequestDto
import com.monglife.mongs.data.mong.web.client.response.ClaimMissionRewardResponseDto
import com.monglife.mongs.data.mong.web.client.response.GetMissionResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MissionWebClient {

    @GET("character/mission")
    suspend fun getMissions(): Response<ResponseDto<List<GetMissionResponseDto>>>

    @POST("character/mission/{accountMissionId}/claim")
    suspend fun claimMissionReward(
        @Path("accountMissionId") accountMissionId: Long,
        @Body claimMissionRewardRequestDto: ClaimMissionRewardRequestDto,
    ): Response<ResponseDto<ClaimMissionRewardResponseDto>>
}

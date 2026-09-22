package com.monglife.mongs.data.battle.web.client

import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.data.battle.web.client.response.GetMatchOutcomeResponseDto
import com.monglife.mongs.data.battle.web.client.response.GetOverMatchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MatchWebClient {

    @GET("character/battle/match")
    suspend fun getMatchOutcome(): Response<ResponseDto<GetMatchOutcomeResponseDto>>

    @GET("character/battle/match/over/{matchId}")
    suspend fun getOverMatch(@Path("matchId") matchId: Long): Response<ResponseDto<GetOverMatchResponseDto>>
}
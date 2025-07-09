package com.monglife.mongs.data.battle.web.client

import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.data.battle.web.client.response.GetMatchOutcomeResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface MatchWebClient {

    @GET("character/battle/match")
    suspend fun getMatchOutcome(): Response<ResponseDto<GetMatchOutcomeResponseDto>>
}
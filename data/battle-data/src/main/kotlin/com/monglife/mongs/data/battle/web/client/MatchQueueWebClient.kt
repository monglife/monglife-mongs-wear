package com.monglife.mongs.data.battle.web.client

import com.monglife.core.data.web.dto.response.ResponseDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface MatchQueueWebClient {

    @POST("character/battle/queue/{mongId}")
    suspend fun createQueuePlayer(@Path("mongId") mongId: Long): Response<ResponseDto<Void>>

    @DELETE("character/battle/queue/{mongId}")
    suspend fun deleteQueuePlayer(@Path("mongId") mongId: Long): Response<ResponseDto<Void>>
}
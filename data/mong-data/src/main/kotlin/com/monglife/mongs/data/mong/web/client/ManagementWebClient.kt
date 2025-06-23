package com.monglife.mongs.data.mong.web.client

import com.monglife.mongs.data.core.dto.response.ResponseDto
import com.monglife.mongs.data.mong.web.client.request.CreateMongRequestDto
import com.monglife.mongs.data.mong.web.client.response.CreateMongResponseDto
import com.monglife.mongs.data.mong.web.client.response.DeleteMongResponseDto
import com.monglife.mongs.data.mong.web.client.response.EvolutionMongResponseDto
import com.monglife.mongs.data.mong.web.client.response.GetMongResponseDto
import com.monglife.mongs.data.mong.web.client.response.GraduateMongResponseDto
import com.monglife.mongs.data.mong.web.client.response.PoopCleanMongResponseDto
import com.monglife.mongs.data.mong.web.client.response.SleepWakeupResponseDto
import com.monglife.mongs.data.mong.web.client.response.StrokeMongResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ManagementWebClient {

    /**
     * 몽 목록 조회 API 요청
     */
    @GET("management")
    suspend fun getMongs(): Response<ResponseDto<List<GetMongResponseDto>>>

    /**
     * 몽 조회 API 요청
     */
    @GET("management/{mongId}")
    suspend fun getMong(@Path("mongId") mongId: Long): Response<ResponseDto<GetMongResponseDto>>

    /**
     * 몽 생성 API 요청
     */
    @POST("management")
    suspend fun createMong(@Body createMongRequestDto: CreateMongRequestDto): Response<ResponseDto<CreateMongResponseDto>>

    /**
     * 몽 삭제 API 요청
     */
    @DELETE("management/{mongId}")
    suspend fun deleteMong(@Path("mongId") mongId: Long): Response<ResponseDto<DeleteMongResponseDto>>

    /**
     * 몽 쓰다 듬기 API 요청
     */
    @POST("management/stroke/{mongId}")
    suspend fun strokeMong(@Path("mongId") mongId: Long): Response<ResponseDto<StrokeMongResponseDto>>

    /**
     * 몽 수면/기상 API 요청
     */
    @PUT("management/sleep/{mongId}")
    suspend fun sleepMong(@Path("mongId") mongId: Long): Response<ResponseDto<SleepWakeupResponseDto>>

    /**
     * 몽 배변 처리 API 요청
     */
    @POST("management/poopClean/{mongId}")
    suspend fun poopCleanMong(@Path("mongId") mongId: Long): Response<ResponseDto<PoopCleanMongResponseDto>>

    /**
     * 몽 진화 API 요청
     */
    @PUT("management/evolution/{mongId}")
    suspend fun evolutionMong(@Path("mongId") mongId: Long): Response<ResponseDto<EvolutionMongResponseDto>>

    /**
     * 몽 졸업 API 요청
     */
    @PUT("management/graduate/{mongId}")
    suspend fun graduateMong(@Path("mongId") mongId: Long): Response<ResponseDto<GraduateMongResponseDto>>
}
package com.monglife.mongs.data.mong.web.client

import com.monglife.mongs.data.core.web.dto.response.PageResponseDto
import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import com.monglife.mongs.data.mong.web.client.request.FeedFoodRequestDto
import com.monglife.mongs.data.mong.web.client.request.FeedSnackRequestDto
import com.monglife.mongs.data.mong.web.client.request.UseInventoryRequestDto
import com.monglife.mongs.data.mong.web.client.response.BuyRandomDrawTicketResponseDto
import com.monglife.mongs.data.mong.web.client.response.FeedFoodResponseDto
import com.monglife.mongs.data.mong.web.client.response.FeedSnackResponseDto
import com.monglife.mongs.data.mong.web.client.response.GetFoodResponseDto
import com.monglife.mongs.data.mong.web.client.response.GetInventoryResponseDto
import com.monglife.mongs.data.mong.web.client.response.GetSnackResponseDto
import com.monglife.mongs.data.mong.web.client.response.RandomDrawResponseDto
import com.monglife.mongs.data.mong.web.client.response.UseInventoryResponseDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InteractionWebClientModule {
    @Provides
    @Singleton
    fun provideInteractionWebClient(@Named("monglife-mongs") retrofit: Retrofit): InteractionWebClient =
        retrofit.create(InteractionWebClient::class.java)
}

interface InteractionWebClient {

    /**
     * 몽 먹이 목록 조회 API 요청
     */
    @GET("character/interaction/food/{mongId}")
    suspend fun getFoods(@Path("mongId") mongId: Long): Response<ResponseDto<List<GetFoodResponseDto>>>

    /**
     * 몽 간식 목록 조회 API 요청
     */
    @GET("character/interaction/snack/{mongId}")
    suspend fun getSnacks(@Path("mongId") mongId: Long): Response<ResponseDto<List<GetSnackResponseDto>>>

    /**
     * 몽 먹이 주기 API 요청
     */
    @POST("character/interaction/food/{mongId}")
    suspend fun feedFood(@Path("mongId") mongId: Long, @Body feedFoodRequestDto: FeedFoodRequestDto): Response<ResponseDto<FeedFoodResponseDto>>

    /**
     * 몽 간식 주기 API 요청
     */
    @POST("character/interaction/snack/{mongId}")
    suspend fun feedSnack(@Path("mongId") mongId: Long, @Body feedSnackRequestDto: FeedSnackRequestDto): Response<ResponseDto<FeedSnackResponseDto>>

    /**
     * 인벤토리 목록 조회 API 요청
     */
    @GET("character/interaction/inventory/{mongId}")
    suspend fun getInventories(@Path("mongId") mongId: Long, @Query("page") page: Int, @Query("size") size: Int): Response<PageResponseDto<List<GetInventoryResponseDto>>>

    /**
     * 인벤토리 소비 API 요청
     */
    @POST("character/interaction/inventory/{mongId}")
    suspend fun useInventory(@Path("mongId") mongId: Long, @Body useInventoryRequestDto: UseInventoryRequestDto): Response<ResponseDto<UseInventoryResponseDto>>

    /**
     * 랜덤 뽑기 티켓 구매 API 요청
     */
    @POST("character/interaction/randomDraw/ticket/{mongId}")
    suspend fun buyRandomDrawTicket(@Path("mongId") mongId: Long): Response<ResponseDto<BuyRandomDrawTicketResponseDto>>

    /**
     * 랜덤 뽑기 API 요청
     */
    @POST("character/interaction/randomDraw/{mongId}")
    suspend fun randomDraw(@Path("mongId") mongId: Long): Response<ResponseDto<RandomDrawResponseDto>>
}
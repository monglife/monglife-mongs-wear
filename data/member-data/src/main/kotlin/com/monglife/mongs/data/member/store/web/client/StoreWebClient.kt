package com.monglife.mongs.data.member.store.web.client

import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.store.web.client.request.ConsumeOrderRequestDto
import com.monglife.mongs.data.member.store.web.client.request.GetConsumedOrdersRequestDto
import com.monglife.mongs.data.member.store.web.client.response.ConsumeOrderResponseDto
import com.monglife.mongs.data.member.store.web.client.response.GetConsumedOrderResponseDto
import com.monglife.mongs.data.member.store.web.client.response.GetProductResponseDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StoreWebClientModule {
    @Provides
    @Singleton
    fun provideStoreWebClient(@Named("monglife-mongs") retrofit: Retrofit): StoreWebClient =
        retrofit.create(StoreWebClient::class.java)
}

interface StoreWebClient {

    /**
     * 인앱 상품 목록 조회 API 요청
     */
    @GET("user/store/product")
    suspend fun getProducts(): Response<ResponseDto<List<GetProductResponseDto>>>

    /**
     * 소비된 주문 목록 조회 API 요청
     */
    @POST("user/store/order")
    suspend fun getConsumedOrders(@Body getConsumedOrdersRequestDto: GetConsumedOrdersRequestDto): Response<ResponseDto<List<GetConsumedOrderResponseDto>>>

    /**
     * 주문 소비 API 요청
     */
    @POST("user/store/order/consume")
    suspend fun consumeOrder(@Body consumeOrderRequestDto: ConsumeOrderRequestDto): Response<ResponseDto<ConsumeOrderResponseDto>>
}
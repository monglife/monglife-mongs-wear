package com.monglife.mongs.data.member.collection.web.client

import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.collection.web.client.response.GetCollectionMapResponseDto
import com.monglife.mongs.data.member.collection.web.client.response.GetCollectionMongResponseDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CollectionWebClientModule {
    @Provides
    @Singleton
    fun provideCollectionWebClient(
        @Named("monglife-mongs") retrofit: Retrofit
    ): CollectionWebClient = retrofit.create(CollectionWebClient::class.java)
}

interface CollectionWebClient {

    /**
     * 컬렉션 맵 목록 조회 API 호출
     */
    @GET("user/collection/map")
    suspend fun getCollectionMaps(): Response<ResponseDto<List<GetCollectionMapResponseDto>>>

    /**
     * 컬렉션 몽 목록 조회 API 호출
     */
    @GET("user/collection/mong")
    suspend fun getCollectionMongs(): Response<ResponseDto<List<GetCollectionMongResponseDto>>>
}
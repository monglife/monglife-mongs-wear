package com.monglife.mongs.data.member.collection.web.client

import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.collection.web.client.response.GetCollectionMapResponseDto
import com.monglife.mongs.data.member.collection.web.client.response.GetCollectionMongResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface CollectionWebClient {

    /**
     * 컬렉션 맵 목록 조회 API 호출
     */
    @GET("collection/map")
    suspend fun getCollectionMaps(): Response<ResponseDto<List<GetCollectionMapResponseDto>>>

    /**
     * 컬렉션 몽 목록 조회 API 호출
     */
    @GET("collection/mong")
    suspend fun getCollectionMongs(): Response<ResponseDto<List<GetCollectionMongResponseDto>>>
}
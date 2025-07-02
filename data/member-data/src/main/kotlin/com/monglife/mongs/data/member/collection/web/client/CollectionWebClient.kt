package com.monglife.mongs.data.member.collection.web.client

import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.collection.web.client.response.GetCollectionMapResponseDto
import com.monglife.mongs.data.member.collection.web.client.response.GetCollectionMongResponseDto
import retrofit2.Response
import retrofit2.http.GET

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
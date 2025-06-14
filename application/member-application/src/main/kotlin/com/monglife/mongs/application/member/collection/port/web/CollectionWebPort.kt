package com.monglife.mongs.application.member.collection.port.web

import com.monglife.mongs.application.member.collection.port.web.response.GetCollectionMapResponse
import com.monglife.mongs.application.member.collection.port.web.response.GetCollectionMongResponse

interface CollectionWebPort {

    /**
     * 컬렉션 맵 목록 조회
     */
    suspend fun getCollectionMaps(): List<GetCollectionMapResponse>

    /**
     * 컬렉션 몽 목록 조회
     */
    suspend fun getCollectionMongs(): List<GetCollectionMongResponse>
}
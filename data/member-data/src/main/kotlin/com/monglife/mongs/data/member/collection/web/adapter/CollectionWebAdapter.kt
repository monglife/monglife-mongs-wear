package com.monglife.mongs.data.member.collection.web.adapter

import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import com.monglife.mongs.application.member.collection.port.web.response.GetCollectionMapResponse
import com.monglife.mongs.application.member.collection.port.web.response.GetCollectionMongResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionWebAdapter @Inject constructor(

) : CollectionWebPort {

    /**
     * 컬렉션 맵 목록 조회
     */
    override suspend fun getCollectionMaps(): List<GetCollectionMapResponse> {
        TODO("Not yet implemented")
    }

    /**
     * 컬렉션 몽 목록 조회
     */
    override suspend fun getCollectionMongs(): List<GetCollectionMongResponse> {
        TODO("Not yet implemented")
    }
}
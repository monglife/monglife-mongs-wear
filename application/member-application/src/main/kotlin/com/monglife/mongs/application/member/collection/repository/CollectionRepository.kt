package com.monglife.mongs.domain.member.collection.repository

import com.monglife.mongs.domain.member.collection.model.Collection

interface CollectionRepository {

    /**
     * 컬렉션 맵 등록
     */
    suspend fun createMapCollection(latitude: Double, longitude: Double)

    /**
     * 컬렉션 맵 목록 조회
     */
    suspend fun getMapCollections(): List<Collection>

    /**
     * 컬렉션 몽 목록 조회
     */
    suspend fun getMongCollections(): List<Collection>
}
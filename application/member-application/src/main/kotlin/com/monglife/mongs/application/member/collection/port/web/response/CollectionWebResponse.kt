package com.monglife.mongs.application.member.collection.port.web.response

import com.monglife.mongs.domain.member.collection.model.CollectionMap
import com.monglife.mongs.domain.member.collection.model.CollectionMong

/**
 * 컬렉션 맵 조회 응답
 */
data class GetCollectionMapResponse(
    val mapCode: String,
    val mapName: String,
    val isIncluded: Boolean,
) {
    /**
     * 응답 도메인 변환
     */
    fun toDomain() = CollectionMap(
        code = mapCode,
        name = mapName,
        isIncluded = isIncluded,
    )
}

/**
 * 컬렉션 몽 조회 응답
 */
data class GetCollectionMongResponse(
    val mongCode: String,
    val mongName: String,
    val isIncluded: Boolean,
) {
    /**
     * 응답 도메인 변환
     */
    fun toDomain() = CollectionMong(
        code = mongCode,
        name = mongName,
        isIncluded = isIncluded,
    )
}
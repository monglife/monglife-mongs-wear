package com.monglife.mongs.data.member.collection.web.client.response

/**
 * 컬렉션 맵 조회 응답 Dto
 */
data class GetCollectionMapResponseDto(
    val mapCode: String,
    val mapName: String,
    val isIncluded: Boolean,
)

/**
 * 컬렉션 몽 조회 응답 Dto
 */
data class GetCollectionMongResponseDto(
    val mongCode: String,
    val mongName: String,
    val isIncluded: Boolean,
)

/**
 * 맵 탐색 응답 Dto
 */
data class SearchCollectionMapResponseDto(
    val isFound: Boolean,
    val data: GetCollectionMapResponseDto?,
)
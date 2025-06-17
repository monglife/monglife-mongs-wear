package com.monglife.mongs.data.core.retrofit.client.response

/**
 * 토큰 재발행 응답 Dto
 */
data class ReissueResponseDto(
    val accessToken: String,
    val refreshToken: String,
)
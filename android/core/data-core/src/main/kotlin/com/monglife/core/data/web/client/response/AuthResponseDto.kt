package com.monglife.core.data.web.client.response

/**
 * 앱 버전 검증 응답 Dto
 */
data class VerifyAppVersionResponseDto(
    val appPackageName: String,
    val buildVersion: String,
    val mustUpdate: Boolean,
)

/**
 * 로그인 응답 Dto
 */
data class LoginResponseDto(
    val accountId: Long,
    val accessToken: String,
    val refreshToken: String,
)

/**
 * 토큰 재발행 응답 Dto
 */
data class ReissueResponseDto(
    val accessToken: String,
    val refreshToken: String,
)
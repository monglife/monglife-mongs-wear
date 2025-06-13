package com.monglife.mongs.application.auth.port.web

/**
 * 앱 버전 검증 응답
 */
data class VerifyAppVersionResponse(
    val appPackageName: String,
    val buildVersion: String,
    val mustUpdate: Boolean,
)

/**
 * 로그인 응답
 */
data class LoginResponse(
    val accountId: Long,
    val accessToken: String,
    val refreshToken: String,
)

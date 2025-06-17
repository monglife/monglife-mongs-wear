package com.monglife.mongs.data.auth.web.client.request

/**
 * 회원 가입 요청 Dto
 */
data class JoinRequestDto(
    val email: String,
    val name: String,
    val socialAccountId: String,
)

/**
 * 로그인 요청 Dto
 */
data class LoginRequestDto(
    val deviceId: String,
    val email: String,
    val socialAccountId: String,
    val appPackageName: String,
    val deviceName: String,
    val buildVersion: String
)

/**
 * 로그 아웃 요청 Dto
 */
data class LogoutRequestDto(
    val refreshToken: String,
)
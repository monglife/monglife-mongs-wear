package com.monglife.core.data.web.client.request

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
 * 회원 가입 요청 Dto (Credential 계약)
 *
 * 기존 Dto 와 달리 구글이 서명한 idToken 을 함께 보낸다.
 * 서버가 서명·aud·exp 를 직접 검증할 수 있어 socialAccountId 를 그대로 믿지 않아도 된다.
 */
data class CredentialJoinRequestDto(
    val email: String,
    val name: String,
    val socialAccountId: String,
    val idToken: String,
)

/**
 * 로그인 요청 Dto (Credential 계약)
 */
data class CredentialLoginRequestDto(
    val deviceId: String,
    val email: String,
    val socialAccountId: String,
    val idToken: String,
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

/**
 * 토큰 재발행 요청 Dto
 */
data class ReissueRequestDto(
    val accessToken: String,
    val refreshToken: String
)

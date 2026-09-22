package com.monglife.mongs.application.auth.port.web.response

/**
 * 앱 버전 검증 응답
 *
 * 앱 진입 관문의 응답이다. 강제 업데이트와 서버 점검이 같이 실려 온다.
 * 점검 중이 아니면 maintenance* 는 전부 null 이다.
 */
data class VerifyAppVersionResponse(
    val appPackageName: String,
    val buildVersion: String,
    val mustUpdate: Boolean,
    val underMaintenance: Boolean,
    val maintenanceMessage: String?,
    val maintenanceStartAt: String?,
    val maintenanceEndAt: String?,
)

/**
 * 로그인 응답
 */
data class LoginResponse(
    val accountId: Long,
    val accessToken: String,
    val refreshToken: String,
)

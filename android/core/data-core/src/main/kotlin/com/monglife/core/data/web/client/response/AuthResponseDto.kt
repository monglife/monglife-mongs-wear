package com.monglife.core.data.web.client.response

/**
 * 앱 버전 검증 응답 Dto
 *
 * 서버 점검 정보가 같은 응답에 실려 온다. 점검 필드는 서버가 나중에 더한 것이라
 * **전부 nullable 이어야 한다.** Gson 은 코틀린 기본값을 무시하고 없는 필드를 null 로 채우므로,
 * 비널 타입으로 두면 구 서버에 붙었을 때 읽는 순간 NPE 가 난다.
 * (Boolean? 이 null 이면 점검이 아닌 것으로 본다)
 */
data class VerifyAppVersionResponseDto(
    val appPackageName: String,
    val buildVersion: String,
    val mustUpdate: Boolean,
    val underMaintenance: Boolean?,
    val maintenanceMessage: String?,
    val maintenanceStartAt: String?,
    val maintenanceEndAt: String?,
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
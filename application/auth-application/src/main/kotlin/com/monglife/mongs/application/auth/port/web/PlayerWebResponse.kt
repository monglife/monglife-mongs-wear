package com.monglife.mongs.application.auth.port.web

/**
 * 플레이어 조회 응답
 */
data class GetPlayerResponse(
    val accountId: Long,
    val slotCount: Int,
    val starPoint: Int,
)

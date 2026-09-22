package com.monglife.mongs.data.member.player.persistence.dto

/**
 * 스타 포인트 변경 이벤트 Dto
 */
data class PlayerStarPointEventDto(
    val accountId: Long,
    val starPoint: Int,
)

/**
 * 슬롯 수 변경 이벤트 Dto
 */
data class PlayerSlotCountEventDto(
    val accountId: Long,
    val slotCount: Int,
)
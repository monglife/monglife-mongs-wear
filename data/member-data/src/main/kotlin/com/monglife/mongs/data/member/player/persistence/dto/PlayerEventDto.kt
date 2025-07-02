package com.monglife.mongs.data.member.player.persistence.dto

data class PlayerStarPointEventDto(
    val accountId: Long,
    val starPoint: Int,
)

data class PlayerSlotCountEventDto(
    val accountId: Long,
    val slotCount: Int,
)
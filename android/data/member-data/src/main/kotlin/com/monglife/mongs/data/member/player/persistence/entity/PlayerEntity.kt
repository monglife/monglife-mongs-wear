package com.monglife.mongs.data.member.player.persistence.entity

import com.monglife.mongs.domain.member.player.model.Player

data class PlayerEntity(
    val accountId: Long,
    val slotCount: Int,
    val starPoint: Int,
) {
    /**
     * 엔티티 도메인 변환
     */
    fun toDomain() = Player(
        accountId = this.accountId,
        slotCount = this.slotCount,
        starPoint = this.starPoint,
    )
}

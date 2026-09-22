package com.monglife.mongs.application.member.player.vo

import com.monglife.mongs.domain.member.player.model.Player

data class PlayerVo(
    val accountId: Long,
    val slotCount: Int,
    val starPoint: Int,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(player: Player) = PlayerVo(
            accountId = player.accountId,
            slotCount = player.slotCount,
            starPoint = player.starPoint
        )
    }
}

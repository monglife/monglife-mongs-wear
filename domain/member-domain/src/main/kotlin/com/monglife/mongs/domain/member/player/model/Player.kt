package com.monglife.mongs.domain.member.player.model

class Player(
    accountId: Long,
    slotCount: Int,
    starPoint: Int,
) {
    var accountId: Long = accountId
        private set
    var slotCount: Int = slotCount
        private set
    var starPoint: Int = starPoint
}

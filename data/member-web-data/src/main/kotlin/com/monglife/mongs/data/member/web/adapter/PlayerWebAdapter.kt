package com.monglife.mongs.data.member.web.adapter

import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.application.member.player.port.web.response.BuySlotResponse
import com.monglife.mongs.application.member.player.port.web.response.ExchangeStarPointResponse
import com.monglife.mongs.application.member.player.port.web.response.GetPlayerResponse
import javax.inject.Inject

class PlayerWebAdapter @Inject constructor(

) : PlayerWebPort {
    
    override suspend fun createPlayer() {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayer(): GetPlayerResponse {
        TODO("Not yet implemented")
    }

    override suspend fun buySlot(): BuySlotResponse {
        TODO("Not yet implemented")
    }

    override suspend fun exchangeStarPoint(
        mongId: Long,
        starPoint: Int
    ): ExchangeStarPointResponse {
        TODO("Not yet implemented")
    }
}
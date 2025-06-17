package com.monglife.mongs.data.member.player.web.adapter

import com.monglife.mongs.application.member.player.exception.InvalidBuySlotException
import com.monglife.mongs.application.member.player.exception.InvalidCreatePlayerException
import com.monglife.mongs.application.member.player.exception.InvalidExchangeStarPointException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.application.member.player.port.web.response.BuySlotResponse
import com.monglife.mongs.application.member.player.port.web.response.ExchangeStarPointResponse
import com.monglife.mongs.application.member.player.port.web.response.GetPlayerResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerWebAdapter @Inject constructor(

) : PlayerWebPort {

    /**
     * 플레이어 정보 등록
     */
    @Throws(InvalidCreatePlayerException::class)
    override suspend fun createPlayer() {
        TODO("Not yet implemented")
    }

    /**
     * 플레이어 조회
     */
    @Throws(NotFoundPlayerException::class)
    override suspend fun getPlayer(): GetPlayerResponse {
        TODO("Not yet implemented")
    }

    /**
     * 플레이어 슬롯 구매
     */
    @Throws(InvalidBuySlotException::class)
    override suspend fun buySlot(): BuySlotResponse {
        TODO("Not yet implemented")
    }

    /**
     * 플레이어 스타 포인트 환전
     */
    @Throws(InvalidExchangeStarPointException::class)
    override suspend fun exchangeStarPoint(
        mongId: Long,
        starPoint: Int
    ): ExchangeStarPointResponse {
        TODO("Not yet implemented")
    }
}
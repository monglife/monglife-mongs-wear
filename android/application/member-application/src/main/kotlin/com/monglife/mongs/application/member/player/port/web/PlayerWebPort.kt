package com.monglife.mongs.application.member.player.port.web

import com.monglife.mongs.application.member.player.exception.InvalidBuySlotException
import com.monglife.mongs.application.member.player.exception.InvalidCreatePlayerException
import com.monglife.mongs.application.member.player.exception.InvalidExchangeStarPointException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.web.response.BuySlotResponse
import com.monglife.mongs.application.member.player.port.web.response.ExchangeStarPointResponse
import com.monglife.mongs.application.member.player.port.web.response.GetPlayerResponse

interface PlayerWebPort {

    /**
     * 플레이어 정보 등록
     */
    @Throws(InvalidCreatePlayerException::class)
    suspend fun createPlayer()

    /**
     * 플레이어 조회
     */
    @Throws(NotFoundPlayerException::class)
    suspend fun getPlayer(): GetPlayerResponse

    /**
     * 플레이어 슬롯 구매
     */
    @Throws(InvalidBuySlotException::class)
    suspend fun buySlot(): BuySlotResponse

    /**
     * 플레이어 스타 포인트 환전
     */
    @Throws(InvalidExchangeStarPointException::class)
    suspend fun exchangeStarPoint(mongId: Long, starPoint: Int): ExchangeStarPointResponse
}
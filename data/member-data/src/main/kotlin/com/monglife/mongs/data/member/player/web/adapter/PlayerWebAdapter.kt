package com.monglife.mongs.data.member.player.web.adapter

import com.monglife.mongs.application.member.player.exception.InvalidBuySlotException
import com.monglife.mongs.application.member.player.exception.InvalidCreatePlayerException
import com.monglife.mongs.application.member.player.exception.InvalidExchangeStarPointException
import com.monglife.mongs.application.member.player.exception.NotFoundPlayerException
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.application.member.player.port.web.response.BuySlotResponse
import com.monglife.mongs.application.member.player.port.web.response.ExchangeStarPointResponse
import com.monglife.mongs.application.member.player.port.web.response.GetPlayerResponse
import com.monglife.mongs.data.member.player.web.client.PlayerWebClient
import com.monglife.mongs.data.member.player.web.client.request.ExchangeStartPointRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerWebAdapter @Inject constructor(
    private val playerWebClient: PlayerWebClient,
) : PlayerWebPort {

    /**
     * 플레이어 정보 등록
     */
    @Throws(InvalidCreatePlayerException::class)
    override suspend fun createPlayer(): Unit = playerWebClient.createPlayer().let { response ->
        response.takeIf { it.isSuccessful }?.body() ?: throw InvalidCreatePlayerException()
    }

    /**
     * 플레이어 조회
     */
    @Throws(NotFoundPlayerException::class)
    override suspend fun getPlayer(): GetPlayerResponse =
        playerWebClient.getPlayer().let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw NotFoundPlayerException()

            GetPlayerResponse(
                accountId = body.result.accountId,
                slotCount = body.result.slotCount,
                starPoint = body.result.starPoint,
            )
        }

    /**
     * 플레이어 슬롯 구매
     */
    @Throws(InvalidBuySlotException::class)
    override suspend fun buySlot(): BuySlotResponse = playerWebClient.buySlot().let { response ->

        val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidBuySlotException()

        BuySlotResponse(
            accountId = body.result.accountId,
            slotCount = body.result.slotCount,
            starPoint = body.result.starPoint,
        )
    }

    /**
     * 플레이어 스타 포인트 환전
     */
    @Throws(InvalidExchangeStarPointException::class)
    override suspend fun exchangeStarPoint(
        mongId: Long,
        starPoint: Int
    ): ExchangeStarPointResponse = playerWebClient.exchangeStarPoint(
        exchangeStartPointRequestDto = ExchangeStartPointRequestDto(
            mongId = mongId,
            starPoint = starPoint,
        )
    ).let { response ->

        val body =
            response.takeIf { it.isSuccessful }?.body() ?: throw InvalidExchangeStarPointException()

        ExchangeStarPointResponse(
            accountId = body.result.accountId,
            starPoint = body.result.starPoint,
        )
    }
}
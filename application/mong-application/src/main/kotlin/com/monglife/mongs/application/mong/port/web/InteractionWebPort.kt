package com.monglife.mongs.application.mong.port.web

import com.monglife.mongs.application.mong.exception.InvalidBuyRandomDrawTicketException
import com.monglife.mongs.application.mong.exception.InvalidConsumeInventoryException
import com.monglife.mongs.application.mong.exception.InvalidFeedFoodException
import com.monglife.mongs.application.mong.exception.InvalidFeedSnackException
import com.monglife.mongs.application.mong.exception.InvalidRandomDrawException
import com.monglife.mongs.application.mong.port.web.response.BuyRandomDrawTicketResponse
import com.monglife.mongs.application.mong.port.web.response.ConsumeInventoryResponse
import com.monglife.mongs.application.mong.port.web.response.FeedFoodMongResponse
import com.monglife.mongs.application.mong.port.web.response.FeedSnackMongResponse
import com.monglife.mongs.application.mong.port.web.response.GetFoodResponse
import com.monglife.mongs.application.mong.port.web.response.GetInventoryResponse
import com.monglife.mongs.application.mong.port.web.response.GetSnackResponse
import com.monglife.mongs.application.mong.port.web.response.RandomDrawResponseVo
import com.monglife.core.application.response.PageResponse

interface InteractionWebPort {

    /**
     * 몽 먹이 목록 조회
     */
    suspend fun getFoods(mongId: Long): List<GetFoodResponse>

    /**
     * 몽 간식 목록 조회
     */
    suspend fun getSnacks(mongId: Long): List<GetSnackResponse>

    /**
     * 몽 먹이 주기
     */
    @Throws(InvalidFeedFoodException::class)
    suspend fun feedFoodMong(mongId: Long, foodCode: String): FeedFoodMongResponse

    /**
     * 몽 간식 주기
     */
    @Throws(InvalidFeedSnackException::class)
    suspend fun feedSnackMong(mongId: Long, snackCode: String): FeedSnackMongResponse

    /**
     * 인벤토리 목록 조회
     */
    suspend fun getInventories(mongId: Long, page: Int, size: Int): PageResponse<GetInventoryResponse>

    /**
     * 인벤토리 소비
     */
    @Throws(InvalidConsumeInventoryException::class)
    suspend fun consumeInventory(mongId: Long, inventoryId: Long): ConsumeInventoryResponse

    /**
     * 랜덤 뽑기 티켓 구매
     */
    @Throws(InvalidBuyRandomDrawTicketException::class)
    suspend fun buyRandomDrawTicket(mongId: Long): BuyRandomDrawTicketResponse

    /**
     * 랜덤 뽑기
     */
    @Throws(InvalidRandomDrawException::class)
    suspend fun randomDraw(mongId: Long): RandomDrawResponseVo
}
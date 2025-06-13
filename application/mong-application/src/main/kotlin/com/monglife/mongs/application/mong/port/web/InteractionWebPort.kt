package com.monglife.mongs.application.mong.port.web

import com.monglife.mongs.application.mong.exception.InvalidBuyRandomDrawTicketException
import com.monglife.mongs.application.mong.exception.InvalidConsumeInventoryException
import com.monglife.mongs.application.mong.exception.InvalidFeedFoodException
import com.monglife.mongs.application.mong.exception.InvalidFeedSnackException
import com.monglife.mongs.application.mong.exception.InvalidRandomDrawException

interface InteractionWebPort {

    /**
     * 몽 먹이 목록 조회
     */
    suspend fun getFoods(mongId: Long): List<GetFoodResponseVo>

    /**
     * 몽 간식 목록 조회
     */
    suspend fun getSnacks(mongId: Long): List<GetSnackResponseVo>

    /**
     * 몽 먹이 주기
     */
    @Throws(InvalidFeedFoodException::class)
    suspend fun feedFoodMong(mongId: Long, foodCode: String): FeedFoodMongResponseVo

    /**
     * 몽 간식 주기
     */
    @Throws(InvalidFeedSnackException::class)
    suspend fun feedSnackMong(mongId: Long, snackCode: String): FeedSnackMongResponseVo

    /**
     * 인벤토리 목록 조회
     */
    suspend fun getInventories(mongId: Long): List<GetInventoryResponseVo>

    /**
     * 인벤토리 소비
     */
    @Throws(InvalidConsumeInventoryException::class)
    suspend fun consumeInventory(mongId: Long, inventoryId: Long): ConsumeInventoryResponseVo

    /**
     * 랜덤 뽑기 티켓 구매
     */
    @Throws(InvalidBuyRandomDrawTicketException::class)
    suspend fun buyRandomDrawTicket(mongId: Long): BuyRandomDrawTicketResponseVo

    /**
     * 랜덤 뽑기
     */
    @Throws(InvalidRandomDrawException::class)
    suspend fun randomDraw(mongId: Long): RandomDrawResponseVo
}
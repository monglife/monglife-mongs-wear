package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.exception.InvalidBuyRandomDrawTicketException
import com.monglife.mongs.application.mong.exception.InvalidConsumeInventoryException
import com.monglife.mongs.application.mong.exception.InvalidFeedFoodException
import com.monglife.mongs.application.mong.exception.InvalidFeedSnackException
import com.monglife.mongs.application.mong.exception.InvalidRandomDrawException
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.response.BuyRandomDrawTicketResponse
import com.monglife.mongs.application.mong.port.web.response.ConsumeInventoryResponse
import com.monglife.mongs.application.mong.port.web.response.FeedFoodMongResponse
import com.monglife.mongs.application.mong.port.web.response.FeedSnackMongResponse
import com.monglife.mongs.application.mong.port.web.response.GetFoodResponse
import com.monglife.mongs.application.mong.port.web.response.GetInventoryResponse
import com.monglife.mongs.application.mong.port.web.response.GetSnackResponse
import com.monglife.mongs.application.mong.port.web.response.RandomDrawResponseVo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionWebAdapter @Inject constructor(
    
) : InteractionWebPort {

    /**
     * 몽 먹이 목록 조회
     */
    override suspend fun getFoods(mongId: Long): List<GetFoodResponse> {
        TODO("Not yet implemented")
    }

    /**
     * 몽 간식 목록 조회
     */
    override suspend fun getSnacks(mongId: Long): List<GetSnackResponse> {
        TODO("Not yet implemented")
    }

    /**
     * 몽 먹이 주기
     */
    @Throws(InvalidFeedFoodException::class)
    override suspend fun feedFoodMong(mongId: Long, foodCode: String): FeedFoodMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 몽 간식 주기
     */
    @Throws(InvalidFeedSnackException::class)
    override suspend fun feedSnackMong(mongId: Long, snackCode: String): FeedSnackMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 인벤토리 목록 조회
     */
    override suspend fun getInventories(mongId: Long): List<GetInventoryResponse> {
        TODO("Not yet implemented")
    }

    /**
     * 인벤토리 소비
     */
    @Throws(InvalidConsumeInventoryException::class)
    override suspend fun consumeInventory(
        mongId: Long,
        inventoryId: Long
    ): ConsumeInventoryResponse {
        TODO("Not yet implemented")
    }

    /**
     * 랜덤 뽑기 티켓 구매
     */
    @Throws(InvalidBuyRandomDrawTicketException::class)
    override suspend fun buyRandomDrawTicket(mongId: Long): BuyRandomDrawTicketResponse {
        TODO("Not yet implemented")
    }

    /**
     * 랜덤 뽑기
     */
    @Throws(InvalidRandomDrawException::class)
    override suspend fun randomDraw(mongId: Long): RandomDrawResponseVo {
        TODO("Not yet implemented")
    }
}
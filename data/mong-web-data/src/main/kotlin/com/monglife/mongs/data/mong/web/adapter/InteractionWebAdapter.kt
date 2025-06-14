package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.response.BuyRandomDrawTicketResponseVo
import com.monglife.mongs.application.mong.port.web.response.ConsumeInventoryResponseVo
import com.monglife.mongs.application.mong.port.web.response.FeedFoodMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.FeedSnackMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.GetFoodResponseVo
import com.monglife.mongs.application.mong.port.web.response.GetInventoryResponseVo
import com.monglife.mongs.application.mong.port.web.response.GetSnackResponseVo
import com.monglife.mongs.application.mong.port.web.response.RandomDrawResponseVo
import javax.inject.Inject

class InteractionWebAdapter @Inject constructor(

) : InteractionWebPort {

    override suspend fun getFoods(mongId: Long): List<GetFoodResponseVo> {
        TODO("Not yet implemented")
    }

    override suspend fun getSnacks(mongId: Long): List<GetSnackResponseVo> {
        TODO("Not yet implemented")
    }

    override suspend fun feedFoodMong(mongId: Long, foodCode: String): FeedFoodMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun feedSnackMong(mongId: Long, snackCode: String): FeedSnackMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun getInventories(mongId: Long): List<GetInventoryResponseVo> {
        TODO("Not yet implemented")
    }

    override suspend fun consumeInventory(
        mongId: Long,
        inventoryId: Long
    ): ConsumeInventoryResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun buyRandomDrawTicket(mongId: Long): BuyRandomDrawTicketResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun randomDraw(mongId: Long): RandomDrawResponseVo {
        TODO("Not yet implemented")
    }
}
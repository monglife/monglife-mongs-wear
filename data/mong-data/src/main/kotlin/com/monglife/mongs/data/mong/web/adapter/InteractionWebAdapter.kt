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
import com.monglife.mongs.core.domain.port.response.PageResponse
import com.monglife.mongs.data.mong.web.client.InteractionWebClient
import com.monglife.mongs.data.mong.web.client.request.FeedFoodRequestDto
import com.monglife.mongs.data.mong.web.client.request.FeedSnackRequestDto
import com.monglife.mongs.data.mong.web.client.request.UseInventoryRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionWebAdapter @Inject constructor(
    private val interactionWebClient: InteractionWebClient,
) : InteractionWebPort {

    /**
     * 몽 먹이 목록 조회
     */
    override suspend fun getFoods(mongId: Long): List<GetFoodResponse> =
        interactionWebClient.getFoods(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            body?.result?.map {
                GetFoodResponse(
                    foodCode = it.foodCode,
                    foodName = it.foodName,
                    price = it.price,
                    weight = it.weight,
                    strength = it.strength,
                    satiety = it.satiety,
                    healthy = it.healthy,
                    fatigue = it.fatigue,
                    isCanBuy = it.isCanBuy,
                )
            }
        } ?: emptyList()

    /**
     * 몽 간식 목록 조회
     */
    override suspend fun getSnacks(mongId: Long): List<GetSnackResponse> =
        interactionWebClient.getSnacks(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            body?.result?.map {
                GetSnackResponse(
                    snackCode = it.snackCode,
                    snackName = it.snackName,
                    price = it.price,
                    weight = it.weight,
                    strength = it.strength,
                    satiety = it.satiety,
                    healthy = it.healthy,
                    fatigue = it.fatigue,
                    isCanBuy = it.isCanBuy,
                )
            }
        } ?: emptyList()

    /**
     * 몽 먹이 주기
     */
    @Throws(InvalidFeedFoodException::class)
    override suspend fun feedFoodMong(mongId: Long, foodCode: String): FeedFoodMongResponse =
        interactionWebClient.feedFood(
            mongId = mongId,
            feedFoodRequestDto = FeedFoodRequestDto(foodCode = foodCode)
        ).let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw InvalidFeedFoodException()

            FeedFoodMongResponse(
                mongId = body.result.mongId,
                payPoint = body.result.payPoint,
                expRatio = body.result.expRatio,
                strengthRatio = body.result.strengthRatio,
                healthyRatio = body.result.healthyRatio,
                satietyRatio = body.result.satietyRatio,
                fatigueRatio = body.result.fatigueRatio,
                weight = body.result.weight,
                stateCode = body.result.stateCode,
                statusCode = body.result.statusCode,
            )
        }

    /**
     * 몽 간식 주기
     */
    @Throws(InvalidFeedSnackException::class)
    override suspend fun feedSnackMong(mongId: Long, snackCode: String): FeedSnackMongResponse =
        interactionWebClient.feedSnack(
            mongId = mongId,
            feedSnackRequestDto = FeedSnackRequestDto(snackCode = snackCode)
        ).let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw InvalidFeedSnackException()

            FeedSnackMongResponse(
                mongId = body.result.mongId,
                payPoint = body.result.payPoint,
                expRatio = body.result.expRatio,
                strengthRatio = body.result.strengthRatio,
                healthyRatio = body.result.healthyRatio,
                satietyRatio = body.result.satietyRatio,
                fatigueRatio = body.result.fatigueRatio,
                weight = body.result.weight,
                stateCode = body.result.stateCode,
                statusCode = body.result.statusCode,
            )
        }

    /**
     * 인벤토리 목록 조회
     */
    override suspend fun getInventories(mongId: Long, page: Int, size: Int): PageResponse<GetInventoryResponse> =
        interactionWebClient.getInventories(mongId = mongId, page = page, size = size).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            val result = body?.result?.map {
                GetInventoryResponse(
                    inventoryId = it.inventoryId,
                    mongId = it.mongId,
                    inventoryCode = it.inventoryCode,
                    inventoryName = it.inventoryName,
                    inventoryTypeCode = it.inventoryTypeCode,
                )
            } ?: emptyList()

            PageResponse(
                page = body?.page ?: page,
                size = body?.size ?: size,
                totalPage = body?.totalPage ?: 0,
                isLastPage = body?.isLastPage ?: true,
                result = result,
            )
        }

    /**
     * 인벤토리 소비
     */
    @Throws(InvalidConsumeInventoryException::class)
    override suspend fun consumeInventory(
        mongId: Long,
        inventoryId: Long
    ): ConsumeInventoryResponse = interactionWebClient.useInventory(
        mongId = mongId,
        useInventoryRequestDto = UseInventoryRequestDto(inventoryId = inventoryId)
    ).let { response ->

        val body =
            response.takeIf { it.isSuccessful }?.body() ?: throw InvalidConsumeInventoryException()

        ConsumeInventoryResponse(
            mongId = body.result.mongId,
            payPoint = body.result.payPoint,
            expRatio = body.result.expRatio,
            strengthRatio = body.result.strengthRatio,
            healthyRatio = body.result.healthyRatio,
            satietyRatio = body.result.satietyRatio,
            fatigueRatio = body.result.fatigueRatio,
            weight = body.result.weight,
            stateCode = body.result.stateCode,
            statusCode = body.result.statusCode,
        )
    }

    /**
     * 랜덤 뽑기 티켓 구매
     */
    @Throws(InvalidBuyRandomDrawTicketException::class)
    override suspend fun buyRandomDrawTicket(mongId: Long): BuyRandomDrawTicketResponse =
        interactionWebClient.buyRandomDrawTicket(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()
                ?: throw InvalidBuyRandomDrawTicketException()

            BuyRandomDrawTicketResponse(
                mongId = body.result.mongId,
                payPoint = body.result.payPoint,
                randomDrawTicketCount = body.result.randomDrawTicketCount,
            )
        }

    /**
     * 랜덤 뽑기
     */
    @Throws(InvalidRandomDrawException::class)
    override suspend fun randomDraw(mongId: Long): RandomDrawResponseVo =
        interactionWebClient.randomDraw(mongId = mongId).let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw InvalidRandomDrawException()

            RandomDrawResponseVo(
                randomDrawCode = body.result.randomDrawCode,
                randomDrawName = body.result.randomDrawName,
                inventoryTypeCode = body.result.inventoryTypeCode,
            )
        }
}
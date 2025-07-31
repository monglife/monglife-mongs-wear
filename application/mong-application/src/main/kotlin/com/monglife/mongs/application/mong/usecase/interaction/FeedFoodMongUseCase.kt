package com.monglife.mongs.application.mong.usecase.interaction

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.mong.exception.InvalidFeedFoodException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 먹이 주기 UseCase
 */
class FeedFoodMongUseCase @Inject constructor(
    private val interactionWebPort: InteractionWebPort,
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<FeedFoodMongUseCase.Command, Unit>() {

    @Throws(NotFoundMongException::class, InvalidFeedFoodException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 몽 먹이 섭취 요청
            interactionWebPort.feedFoodMong(
                mongId = command.mongId,
                foodCode = command.foodCode,
            ).let { response ->
                val mong = managementPersistencePort.getMong(mongId = command.mongId)
                    ?: managementWebPort.getMong(mongId = command.mongId).let {
                        managementPersistencePort.saveMong(mong = it.toDomain())
                    }

                mong.let {
                    // 몽 섭취
                    it.feed(
                        payPoint = response.payPoint,
                        expRatio = response.expRatio,
                        strengthRatio = response.strengthRatio,
                        healthyRatio = response.healthyRatio,
                        satietyRatio = response.satietyRatio,
                        fatigueRatio = response.fatigueRatio,
                        weight = response.weight,
                        stateCode = response.stateCode,
                        statusCode = response.statusCode,
                    )
                    // 몽 로컬 등록
                    managementPersistencePort.saveMong(mong = it)
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
        val foodCode: String,
    )
}
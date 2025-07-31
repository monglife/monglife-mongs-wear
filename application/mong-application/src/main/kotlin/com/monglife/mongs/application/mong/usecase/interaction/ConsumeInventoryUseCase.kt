package com.monglife.mongs.application.mong.usecase.interaction

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.mong.exception.InvalidConsumeInventoryException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 인벤토리 소비 UseCase
 */
class ConsumeInventoryUseCase @Inject constructor(
    private val interactionWebPort: InteractionWebPort,
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<ConsumeInventoryUseCase.Command, Unit>() {

    @Throws(NotFoundMongException::class, InvalidConsumeInventoryException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 인벤토리 소비 요청
            interactionWebPort.consumeInventory(
                mongId = command.mongId,
                inventoryId = command.inventoryId
            ).let { response ->

                val mong = managementPersistencePort.getMong(mongId = command.mongId)
                    ?: managementWebPort.getMong(mongId = command.mongId).let {
                        managementPersistencePort.saveMong(mong = it.toDomain())
                    }

                mong.let {
                    // 몽 인벤토리 소비
                    it.consumeInventory(
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
        val inventoryId: Long,
    )
}
package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidConsumeInventoryException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 인벤토리 소비 UseCase
 */
class ConsumeInventoryUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val interactionWebPort: InteractionWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<ConsumeInventoryUseCase.Command, MongVo>() {

    @Throws(NotFoundMongException::class, InvalidConsumeInventoryException::class)
    override suspend fun execute(command: Command): MongVo {
        return withContext(Dispatchers.IO) {
            // 몽 조회 요청
            managementWebPort.getMong(mongId = command.mongId).let {
                val mong = it.toDomain()
                // 인벤토리 소비 요청
                interactionWebPort.consumeInventory(mongId = mong.mongId, inventoryId = command.inventoryId).let { response ->
                    // 몽 인벤토리 소비
                    mong.consumeInventory(
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
                    managementPersistencePort.saveMong(mong = mong)
                    // MongVo 반환
                    MongVo.of(mong = mong)
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
        val inventoryId: Long,
    )
}
package com.monglife.mongs.application.mong.usecase.interaction

import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.vo.InventoryVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 인벤토리 목록 조회 UseCase
 */
class GetInventoriesUseCase @Inject constructor(
    private val interactionWebPort: InteractionWebPort,
) : BaseParamUseCase<GetInventoriesUseCase.Command, List<InventoryVo>>() {

    override suspend fun execute(command: Command): List<InventoryVo> {
        return withContext(Dispatchers.IO) {
            // 인벤토리 목록 조회 요청
            interactionWebPort.getInventories(command.mongId).map { response ->
                // InventoryVo 반환
                InventoryVo.of(response.toDomain())
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}
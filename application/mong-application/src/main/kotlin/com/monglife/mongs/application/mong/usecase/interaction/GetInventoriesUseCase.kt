package com.monglife.mongs.application.mong.usecase.interaction

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.core.application.wrapper.Page
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.vo.InventoryVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 인벤토리 목록 조회 UseCase
 */
class GetInventoriesUseCase @Inject constructor(
    private val interactionWebPort: InteractionWebPort,
) : BaseParamUseCase<GetInventoriesUseCase.Command, Page<InventoryVo>>() {

    override suspend fun execute(command: Command): Page<InventoryVo> {
        return withContext(Dispatchers.IO) {
            // 인벤토리 목록 조회 요청
            interactionWebPort.getInventories(
                mongId = command.mongId,
                page = command.page,
                size = command.size,
            ).let { response ->
                Page(
                    page = response.page,
                    size = response.size,
                    totalPage = response.totalPage,
                    isLastPage = response.isLastPage,
                    result = response.result.map { InventoryVo.of(it.toDomain()) },
                )
            }
        }
    }

    data class Command(
        val mongId: Long,
        val page: Int,
        val size: Int,
    )
}
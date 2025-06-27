package com.monglife.mongs.application.mong.usecase.interaction

import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.vo.InventoryVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.core.domain.wrapper.Page
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
            ).let {
                val result = it.result.map { response ->
                    // InventoryVo 반환
                    InventoryVo.of(response.toDomain())
                }

                Page(
                    result = result,
                    page = it.page,
                    size = it.size,
                    totalPage = it.totalPage,
                    isLastPage = it.isLastPage,
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
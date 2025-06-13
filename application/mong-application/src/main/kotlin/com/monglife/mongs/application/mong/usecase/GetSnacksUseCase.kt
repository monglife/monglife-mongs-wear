package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.vo.SnackVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 간식 목록 조회 UseCase
 */
class GetSnacksUseCase @Inject constructor(
    private val interactionWebPort: InteractionWebPort,
) : BaseParamUseCase<GetSnacksUseCase.Command, List<SnackVo>>() {

    override suspend fun execute(command: Command): List<SnackVo> {
        return withContext(Dispatchers.IO) {
            interactionWebPort.getSnacks(mongId = command.mongId).map { response ->
                SnackVo.of(snack = response.toDomain())
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}
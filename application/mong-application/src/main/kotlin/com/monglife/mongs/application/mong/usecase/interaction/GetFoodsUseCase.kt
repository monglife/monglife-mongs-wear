package com.monglife.mongs.application.mong.usecase.interaction

import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.vo.FoodVo
import com.monglife.core.application.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 먹이 목록 조회 UseCase
 */
class GetFoodsUseCase @Inject constructor(
    private val interactionWebPort: InteractionWebPort,
) : BaseParamUseCase<GetFoodsUseCase.Command, List<FoodVo>>() {

    override suspend fun execute(command: Command): List<FoodVo> {
        return withContext(Dispatchers.IO) {
            // 음식 목록 조회 요청
            interactionWebPort.getFoods(mongId = command.mongId).map { response ->
                // FoodVo 반환
                FoodVo.of(food = response.toDomain())
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}
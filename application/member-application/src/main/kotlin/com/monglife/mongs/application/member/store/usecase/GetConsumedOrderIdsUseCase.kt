package com.monglife.mongs.domain.member.store.usecase

import com.monglife.mongs.domain.member.store.repository.StoreRepository
import com.mongs.wear.core.exception.data.GetConsumedOrderIdsException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.GetConsumedOrderIdsUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 소비 주문 ID 목록 조회 UseCase
 */
class GetConsumedOrderIdsUseCase @Inject constructor(
    private val storeRepository: StoreRepository,
) : BaseParamUseCase<GetConsumedOrderIdsUseCase.Param, List<String>>() {

    override suspend fun execute(param: Param): List<String> {
        return withContext(Dispatchers.IO) {
            storeRepository.getConsumedOrderIds(
                orderIds = param.orderIds,
            )
        }
    }

    data class Param(
        val orderIds: List<String>,
    )

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is GetConsumedOrderIdsException -> throw GetConsumedOrderIdsUseCaseException()

            else -> throw GetConsumedOrderIdsUseCaseException()
        }
    }
}
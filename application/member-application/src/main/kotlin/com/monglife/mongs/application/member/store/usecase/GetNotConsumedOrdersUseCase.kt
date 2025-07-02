package com.monglife.mongs.application.member.store.usecase

import com.monglife.mongs.application.member.store.port.web.StoreWebPort
import com.monglife.mongs.application.member.store.vo.OrderVo
import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.member.store.model.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 소비 전 주문 목록 조회 UseCase
 */
class GetNotConsumedOrdersUseCase @Inject constructor(
    private val storeWebPort: StoreWebPort,
) : BaseNoParamUseCase<List<OrderVo>>() {

    override suspend fun execute(): List<OrderVo> {
        return withContext(Dispatchers.IO) {
            // 소비 주문 목록 조회 요청
            storeWebPort.getNotConsumedOrders().map { response ->
                val order = Order(
                    socialOrderId = response.socialOrderId,
                    productId = response.productId,
                    purchaseToken = response.purchaseToken,
                )
                // OrderVo 반환
                OrderVo.of(order = order)
            }
        }
    }
}
package com.monglife.mongs.application.member.store.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.member.store.port.web.StoreWebPort
import com.monglife.mongs.application.member.store.vo.OrderVo
import com.monglife.mongs.application.member.store.vo.ProductVo
import com.monglife.mongs.domain.member.store.model.Order
import com.monglife.mongs.domain.member.store.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 인앱 상품 목록 조회 UseCase
 */
class GetProductsUseCase @Inject constructor(
    private val storeWebPort: StoreWebPort,
) : BaseNoParamUseCase<List<ProductVo>>() {

    override suspend fun execute(): List<ProductVo> {
        return withContext(Dispatchers.IO) {
            val notConsumedOrderVos = storeWebPort.getNotConsumedOrders().map { response ->
                OrderVo.of(
                    order = Order(
                        socialOrderId = response.socialOrderId,
                        productId = response.productId,
                        purchaseToken = response.purchaseToken,
                    )
                )
            }

            // 인앱 상품 목록 조회 요청
            storeWebPort.getProducts().map { response ->
                val product = Product(
                    productId = response.productId,
                    productName = response.productName,
                    price = response.price,
                )

                // ProductVo 반환
                ProductVo.of(
                    product = product,
                    orderVos = notConsumedOrderVos.filter {
                        it.productId == product.productId
                    }
                )
            }
        }
    }
}
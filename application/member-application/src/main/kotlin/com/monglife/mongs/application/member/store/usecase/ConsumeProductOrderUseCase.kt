package com.monglife.mongs.application.member.store.usecase

import com.monglife.mongs.application.member.store.exception.InvalidSubscribePlayerException
import com.monglife.mongs.application.member.store.port.web.StoreWebPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 인앱 상품 주문 소비 UseCase
 */
class ConsumeProductOrderUseCase @Inject constructor(
    private val storeWebPort: StoreWebPort,
) : BaseParamUseCase<ConsumeProductOrderUseCase.Command, Unit>() {

    @Throws(InvalidSubscribePlayerException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 주문 소비 요청
            storeWebPort.consumeOrder(
                productId = command.productId,
                socialOrderId = command.socialOrderId,
                purchaseToken = command.purchaseToken,
            )
        }
    }

    data class Command(
        val productId: String,
        val socialOrderId: String,
        val purchaseToken: String,
    )
}
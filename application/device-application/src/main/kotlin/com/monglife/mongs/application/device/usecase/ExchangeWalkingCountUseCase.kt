package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.device.exception.InvalidExchangeWalkingCountException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.domain.device.model.StepExchangeRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 걸음 수 환전 UseCase
 */
class ExchangeWalkingCountUseCase @Inject constructor(
    private val deviceWebPort: DeviceWebPort,
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<ExchangeWalkingCountUseCase.Command, Unit>() {

    @Throws(InvalidExchangeWalkingCountException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            val walkingCount = StepExchangeRate.walkingCountOf(command.exchangeUnits)

            // 네트워크를 타기 전에 잔액을 먼저 본다. 잔액이 없으면 서버를 부를 이유가 없다.
            if (!devicePersistencePort.getStep().canConsume(walkingCount)) {
                throw InvalidExchangeWalkingCountException()
            }

            // 서버를 먼저 부르고 성공한 뒤에 차감한다. 순서를 뒤집으면 서버 호출이 실패했을 때
            // 사용자의 걸음만 사라진다.
            deviceWebPort.exchangeWalkingCount(
                exchangeWalkingCountRequest = ExchangeWalkingCountRequest(
                    mongId = command.mongId,
                    walkingCount = walkingCount,
                ),
            )

            devicePersistencePort.consumeWalkingCount(walkingCount = walkingCount)
        }
    }

    data class Command(
        val mongId: Long,
        val exchangeUnits: Int,
    )
}

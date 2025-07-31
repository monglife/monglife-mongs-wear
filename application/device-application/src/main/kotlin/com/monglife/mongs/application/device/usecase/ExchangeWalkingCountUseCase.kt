package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.device.exception.InvalidExchangeWalkingCountException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
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
            devicePersistencePort.getStep().let { step ->
                // 걸음 수 환전 요청
                deviceWebPort.exchangeWalkingCount(
                    exchangeWalkingCountRequest = ExchangeWalkingCountRequest(
                        mongId = command.mongId,
                        walkingCount = command.walkingCount,
                        totalWalkingCount = step.totalWalkingCount,
                        deviceBootedAt = step.deviceBootedAt,
                    ),
                ).let {
                    // Step 업데이트
                    step.updateWalkingCount(
                        consumedWalkingCount = it.consumeWalkingCount,
                        walkingCount = it.walkingCount
                    )

                    // Step 로컬 등록
                    devicePersistencePort.saveStep(step = step)
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
        val walkingCount: Int,
    )
}
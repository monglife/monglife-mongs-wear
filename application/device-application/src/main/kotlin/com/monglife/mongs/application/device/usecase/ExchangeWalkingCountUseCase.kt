package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.ExchangeWalkingCountException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 걸음 수 환전 UseCase
 */
class ExchangeWalkingCountUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val deviceWebPort: DeviceWebPort,
) : BaseParamUseCase<ExchangeWalkingCountUseCase.Command, Unit>() {

    @Throws(ExchangeWalkingCountException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // Step 로컬 조회
            val step = devicePersistencePort.getStep()
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

    data class Command(
        val mongId: Long,
        val walkingCount: Int,
    )
}
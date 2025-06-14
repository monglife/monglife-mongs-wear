package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.ExchangeWalkingCountException
import com.monglife.mongs.application.device.exception.NotFoundStepException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.device.model.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
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
            val step = runCatching { devicePersistencePort.getStep() }
                .getOrElse { ex ->
                    // 로컬 Step 이 없는 경우
                    if (ex is NotFoundStepException) {
                        // Step 로컬 등록
                        devicePersistencePort.saveStep(
                            step = Step(
                                totalWalkingCount = command.totalWalkingCount,
                                deviceBootedAt = command.deviceBootedAt,
                            )
                        )
                    } else {
                        throw ex
                    }
                }
            // 걸음 수 환전 요청
            deviceWebPort.exchangeWalkingCount(
                exchangeWalkingCountRequest = ExchangeWalkingCountRequest(
                    mongId = command.mongId,
                    walkingCount = command.walkingCount,
                    totalWalkingCount = command.totalWalkingCount,
                    deviceBootedAt = command.deviceBootedAt,
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
        val totalWalkingCount: Int,
        val walkingCount: Int,
        val deviceBootedAt: LocalDateTime,
    )
}
package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.ExchangeWalkingCountException
import com.monglife.mongs.application.device.exception.NotFoundStepException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
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

    @Throws(NotFoundStepException::class, ExchangeWalkingCountException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 로컬 Step 조회
            val step = runCatching { devicePersistencePort.getStep() }
                .getOrElse {
                    // 로컬 Step 등록
                    devicePersistencePort.saveStep(
                        step = Step(
                            totalWalkingCount = command.totalWalkingCount,
                            deviceBootedAt = command.deviceBootedAt,
                        )
                    )
                }

            // 걸음 수 환전 요청
            deviceWebPort.exchangeWalkingCount(
                mongId = command.mongId,
                walkingCount = command.walkingCount,
                step = step,
            ).let {
                // Step 수정
                step.updateWalkingCount(
                    consumedWalkingCount = it.consumeWalkingCount,
                    walkingCount = it.walkingCount
                )
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
package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundStepException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.device.model.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 총 걸음 수 로컬 저장 UseCase
 */
class SetLocalTotalWalkingCountUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<SetLocalTotalWalkingCountUseCase.Command, Unit>() {

    @Throws(NotFoundStepException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            runCatching { devicePersistencePort.getStep() }
                .getOrElse {
                    // 로컬 Step 등록
                    devicePersistencePort.saveStep(step = Step(
                        totalWalkingCount = command.totalWalkingCount,
                        deviceBootedAt = command.deviceBootedAt,
                    ))
                }
        }
    }

    data class Command(
        val totalWalkingCount: Int,
        val deviceBootedAt: LocalDateTime,
    )
}
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

    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // Step 로컬 조회
            runCatching { devicePersistencePort.getStep() }
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
        }
    }

    data class Command(
        val totalWalkingCount: Int,
        val deviceBootedAt: LocalDateTime,
    )
}
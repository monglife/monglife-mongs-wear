package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundStepException
import com.monglife.mongs.application.device.exception.UpdateWalkingCountException
import com.monglife.mongs.application.device.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.device.model.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 총 걸음 수 서버 동기화 UseCase
 */
class SetRemoteTotalWalkingCountUseCase @Inject constructor(
    private val deviceWebPort: DeviceWebPort,
    private val authPersistencePort: AuthPersistencePort,
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<SetRemoteTotalWalkingCountUseCase.Command, Unit>() {

    @Throws(NotFoundStepException::class, UpdateWalkingCountException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 로그인 여부 체크
            if (authPersistencePort.isExistsSession()) {
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

                // 걸음 수 서버 동기화
                deviceWebPort.updateWalkingCount(step).let {
                    // 걸음 수 로컬 동기화
                    step.updateWalkingCount(
                        consumedWalkingCount = it.consumeWalkingCount,
                        walkingCount = it.walkingCount
                    )
                    devicePersistencePort.saveStep(step = step)
                }
            }
        }
    }

    data class Command(
        val totalWalkingCount: Int,
        val deviceBootedAt: LocalDateTime,
    )
}
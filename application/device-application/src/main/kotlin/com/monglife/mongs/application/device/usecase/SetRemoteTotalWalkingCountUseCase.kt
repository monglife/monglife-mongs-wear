package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.NotFoundStepException
import com.monglife.mongs.application.device.exception.UpdateWalkingCountException
import com.monglife.mongs.application.device.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.UpdateWalkingCountRequest
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

    @Throws(UpdateWalkingCountException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 로그인 여부 체크
            if (authPersistencePort.isExistsSession()) {
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
                    }.let { step: Step ->
                        // Step 서버 동기화
                        deviceWebPort.updateWalkingCount(
                            updateWalkingCountRequest = UpdateWalkingCountRequest(
                                totalWalkingCount = command.totalWalkingCount,
                                deviceBootedAt = command.deviceBootedAt,
                            )
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
    }

    data class Command(
        val totalWalkingCount: Int,
        val deviceBootedAt: LocalDateTime,
    )
}
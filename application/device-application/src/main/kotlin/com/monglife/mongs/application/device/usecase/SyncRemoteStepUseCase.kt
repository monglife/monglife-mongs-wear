package com.monglife.mongs.application.device.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.UpdateWalkingCountRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncRemoteStepUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val deviceWebPort: DeviceWebPort,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            val step = devicePersistencePort.getStep()
            deviceWebPort.updateWalkingCount(
                updateWalkingCountRequest = UpdateWalkingCountRequest(
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
            // 정기 스케줄 등록
            deviceWebPort.createUpdateWalkingCountScheduler()
        }
    }
}
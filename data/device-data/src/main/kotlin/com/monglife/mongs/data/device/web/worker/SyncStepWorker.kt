package com.monglife.mongs.data.device.web.worker

import android.content.Context
import android.os.SystemClock
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.monglife.mongs.application.device.exception.UpdateWalkingCountException
import com.monglife.mongs.data.device.persistence.datastore.DeviceDataStore
import com.monglife.mongs.data.device.persistence.entity.StepEntity
import com.monglife.mongs.data.device.persistence.manager.StepSensorManager
import com.monglife.mongs.data.device.web.client.DeviceWebClient
import com.monglife.mongs.data.device.web.client.request.UpdateTotalWalkingCountRequestDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@HiltWorker
class SyncStepWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val deviceWebClient: DeviceWebClient,
    private val stepSensorManager: StepSensorManager,
    private val deviceDataStore: DeviceDataStore,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORKER_NAME = "SYNC_STEP_WORKER"
    }

    override suspend fun doWork(): Result = try {
        deviceWebClient.updateTotalWalkingCount(
            updateTotalWalkingCountRequestDto = UpdateTotalWalkingCountRequestDto(
                totalWalkingCount = stepSensorManager.getTotalWalkingCount(),
                deviceBootedAt = getBootedAt(),
            )
        ).let { response ->
            val body = response.takeIf { it.isSuccessful }?.body() ?: throw UpdateWalkingCountException()

            deviceDataStore.getStep()?.let {
                deviceDataStore.saveStep(
                    stepEntity = StepEntity(
                        walkingCount = body.result.walkingCount,
                        consumedWalkingCount = body.result.consumeWalkingCount
                    )
                )
            }
        }

        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }

    /**
     * 기기 부팅 시간 조회
     */
    private fun getBootedAt(): LocalDateTime {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
        val uptimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val deviceBootedDt =
            Instant.ofEpochMilli(uptimeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return LocalDateTime.parse(deviceBootedDt.format(dateFormatter), dateFormatter)
    }
}
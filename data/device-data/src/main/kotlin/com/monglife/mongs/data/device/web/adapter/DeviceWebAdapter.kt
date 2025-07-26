package com.monglife.mongs.data.device.web.adapter

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.monglife.mongs.application.device.exception.InvalidExchangeWalkingCountException
import com.monglife.mongs.application.device.exception.NotFoundStepException
import com.monglife.mongs.application.device.exception.UpdateWalkingCountException
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.application.device.port.web.request.UpdateWalkingCountRequest
import com.monglife.mongs.application.device.port.web.response.ExchangeWalkingCountResponse
import com.monglife.mongs.application.device.port.web.response.GetStepResponse
import com.monglife.mongs.application.device.port.web.response.UpdateWalkingCountResponse
import com.monglife.mongs.data.device.web.client.DeviceWebClient
import com.monglife.mongs.data.device.web.client.request.ExchangeCurrentWalkingCountRequestDto
import com.monglife.mongs.data.device.web.client.request.UpdateTotalWalkingCountRequestDto
import com.monglife.mongs.data.device.web.worker.SyncStepWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceWebAdapter @Inject constructor(
    private val deviceWebClient: DeviceWebClient,
    private val workerManager: WorkManager,
) : DeviceWebPort {

    /**
     * 걸음 수 조회
     */
    @Throws(NotFoundStepException::class)
    override suspend fun getStep(): GetStepResponse =
        deviceWebClient.getStep().let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw NotFoundStepException()

            GetStepResponse(
                consumeWalkingCount = body.result.consumeWalkingCount,
                walkingCount = body.result.walkingCount,
            )
        }

    /**
     * 걸음 수 환전
     */
    @Throws(InvalidExchangeWalkingCountException::class)
    override suspend fun exchangeWalkingCount(exchangeWalkingCountRequest: ExchangeWalkingCountRequest): ExchangeWalkingCountResponse =
        deviceWebClient.exchangeCurrentWalkingCount(
            exchangeCurrentWalkingCountRequestDto = ExchangeCurrentWalkingCountRequestDto(
                mongId = exchangeWalkingCountRequest.mongId,
                totalWalkingCount = exchangeWalkingCountRequest.totalWalkingCount,
                walkingCount = exchangeWalkingCountRequest.walkingCount,
                deviceBootedAt = exchangeWalkingCountRequest.deviceBootedAt,
            )
        ).let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw InvalidExchangeWalkingCountException()

            ExchangeWalkingCountResponse(
                consumeWalkingCount = body.result.consumeWalkingCount,
                walkingCount = body.result.walkingCount,
            )
        }

    /**
     * 걸음 수 동기화
     */
    @Throws(UpdateWalkingCountException::class)
    override suspend fun updateWalkingCount(updateWalkingCountRequest: UpdateWalkingCountRequest): UpdateWalkingCountResponse =
        deviceWebClient.updateTotalWalkingCount(
            updateTotalWalkingCountRequestDto = UpdateTotalWalkingCountRequestDto(
                totalWalkingCount = updateWalkingCountRequest.totalWalkingCount,
                deviceBootedAt = updateWalkingCountRequest.deviceBootedAt,
            )
        ).let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw UpdateWalkingCountException()

            UpdateWalkingCountResponse(
                consumeWalkingCount = body.result.consumeWalkingCount,
                walkingCount = body.result.walkingCount,
            )
        }

    /**
     * 걸음 수 동기화 스케줄 생성
     */
    override suspend fun createUpdateWalkingCountScheduler() {
        workerManager.enqueueUniquePeriodicWork(
            SyncStepWorker.WORKER_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<SyncStepWorker>(15, TimeUnit.MINUTES).build()
        )
    }
}
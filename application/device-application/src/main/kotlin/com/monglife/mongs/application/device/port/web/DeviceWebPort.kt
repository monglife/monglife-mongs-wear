package com.monglife.mongs.application.device.port.web

import com.monglife.mongs.application.device.exception.ExchangeWalkingCountException
import com.monglife.mongs.application.device.exception.UpdateWalkingCountException
import com.monglife.mongs.domain.device.model.Step

interface DeviceWebPort {

    /**
     * 걸음 수 환전
     */
    @Throws(ExchangeWalkingCountException::class)
    suspend fun exchangeWalkingCount(mongId: Long, walkingCount: Int, step: Step): ExchangeWalkingCountResponseVo

    /**
     * 걸음 수 동기화
     */
    @Throws(UpdateWalkingCountException::class)
    suspend fun updateWalkingCount(step: Step): UpdateWalkingCountResponseVo
}
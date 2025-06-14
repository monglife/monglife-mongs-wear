package com.monglife.mongs.application.device.port.web

import com.monglife.mongs.application.device.exception.ExchangeWalkingCountException
import com.monglife.mongs.application.device.exception.UpdateWalkingCountException
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.application.device.port.web.request.UpdateWalkingCountRequest
import com.monglife.mongs.application.device.port.web.response.ExchangeWalkingCountResponse
import com.monglife.mongs.application.device.port.web.response.UpdateWalkingCountResponse
import com.monglife.mongs.domain.device.model.Step

interface DeviceWebPort {

    /**
     * 걸음 수 환전
     */
    @Throws(ExchangeWalkingCountException::class)
    suspend fun exchangeWalkingCount(exchangeWalkingCountRequest: ExchangeWalkingCountRequest): ExchangeWalkingCountResponse

    /**
     * 걸음 수 동기화
     */
    @Throws(UpdateWalkingCountException::class)
    suspend fun updateWalkingCount(updateWalkingCountRequest: UpdateWalkingCountRequest): UpdateWalkingCountResponse
}
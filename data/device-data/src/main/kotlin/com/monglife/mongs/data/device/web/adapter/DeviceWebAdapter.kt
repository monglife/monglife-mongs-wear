package com.monglife.mongs.data.device.web.adapter

import com.monglife.mongs.application.device.exception.ExchangeWalkingCountException
import com.monglife.mongs.application.device.exception.UpdateWalkingCountException
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.application.device.port.web.request.UpdateWalkingCountRequest
import com.monglife.mongs.application.device.port.web.response.ExchangeWalkingCountResponse
import com.monglife.mongs.application.device.port.web.response.UpdateWalkingCountResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceWebAdapter @Inject constructor(

) : DeviceWebPort {

    /**
     * 걸음 수 환전
     */
    @Throws(ExchangeWalkingCountException::class)
    override suspend fun exchangeWalkingCount(exchangeWalkingCountRequest: ExchangeWalkingCountRequest): ExchangeWalkingCountResponse {
        TODO("Not yet implemented")
    }

    /**
     * 걸음 수 동기화
     */
    @Throws(UpdateWalkingCountException::class)
    override suspend fun updateWalkingCount(updateWalkingCountRequest: UpdateWalkingCountRequest): UpdateWalkingCountResponse {
        TODO("Not yet implemented")
    }
}
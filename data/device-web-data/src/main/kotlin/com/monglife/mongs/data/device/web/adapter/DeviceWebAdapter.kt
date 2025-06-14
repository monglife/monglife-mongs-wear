package com.monglife.mongs.data.device.web.adapter

import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.application.device.port.web.request.UpdateWalkingCountRequest
import com.monglife.mongs.application.device.port.web.response.ExchangeWalkingCountResponse
import com.monglife.mongs.application.device.port.web.response.UpdateWalkingCountResponse
import javax.inject.Inject

class DeviceWebAdapter @Inject constructor(

) : DeviceWebPort {

    override suspend fun exchangeWalkingCount(exchangeWalkingCountRequest: ExchangeWalkingCountRequest): ExchangeWalkingCountResponse {
        TODO("Not yet implemented")
    }

    override suspend fun updateWalkingCount(updateWalkingCountRequest: UpdateWalkingCountRequest): UpdateWalkingCountResponse {
        TODO("Not yet implemented")
    }
}
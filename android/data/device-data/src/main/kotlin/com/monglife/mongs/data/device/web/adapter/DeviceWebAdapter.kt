package com.monglife.mongs.data.device.web.adapter

import com.monglife.mongs.application.device.exception.InvalidExchangeWalkingCountException
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.data.device.web.client.DeviceWebClient
import com.monglife.mongs.data.device.web.client.request.ExchangeCurrentWalkingCountRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceWebAdapter @Inject constructor(
    private val deviceWebClient: DeviceWebClient,
) : DeviceWebPort {

    /**
     * 걸음 수 환전
     */
    @Throws(InvalidExchangeWalkingCountException::class)
    override suspend fun exchangeWalkingCount(exchangeWalkingCountRequest: ExchangeWalkingCountRequest) {
        val response = deviceWebClient.exchangeCurrentWalkingCount(
            exchangeCurrentWalkingCountRequestDto = ExchangeCurrentWalkingCountRequestDto(
                mongId = exchangeWalkingCountRequest.mongId,
                walkingCount = exchangeWalkingCountRequest.walkingCount,
            )
        )

        if (!response.isSuccessful) throw InvalidExchangeWalkingCountException()
    }
}

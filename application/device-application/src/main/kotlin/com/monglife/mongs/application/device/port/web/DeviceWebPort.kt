package com.monglife.mongs.application.device.port.web

import com.monglife.mongs.application.device.exception.InvalidExchangeWalkingCountException
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest

interface DeviceWebPort {

    /**
     * 걸음 수 환전
     *
     * 서버는 걸음 수 잔액을 보관하지 않는다. 요청한 걸음 수만큼 payPoint 를 지급할 뿐이고,
     * 잔액 차감은 기기가 스스로 한다.
     */
    @Throws(InvalidExchangeWalkingCountException::class)
    suspend fun exchangeWalkingCount(exchangeWalkingCountRequest: ExchangeWalkingCountRequest)
}

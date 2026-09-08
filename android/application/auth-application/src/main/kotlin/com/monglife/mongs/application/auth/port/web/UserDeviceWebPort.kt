package com.monglife.mongs.application.auth.port.web

import com.monglife.mongs.application.auth.exception.InvalidCreateUserDeviceException
import com.monglife.mongs.application.auth.port.web.request.CreateDeviceRequest

interface UserDeviceWebPort {

    /**
     * 기기 등록
     */
    @Throws(InvalidCreateUserDeviceException::class)
    suspend fun createDevice(createDeviceRequest: CreateDeviceRequest)
}
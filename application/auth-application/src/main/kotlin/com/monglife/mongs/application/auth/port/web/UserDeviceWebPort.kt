package com.monglife.mongs.application.auth.port.web

import com.monglife.mongs.application.auth.exception.InvalidCreateUserDeviceException
import com.monglife.mongs.domain.auth.model.UserDevice

interface UserDeviceWebPort {

    /**
     * 기기 등록
     */
    @Throws(InvalidCreateUserDeviceException::class)
    suspend fun createDevice(userDevice: UserDevice)
}
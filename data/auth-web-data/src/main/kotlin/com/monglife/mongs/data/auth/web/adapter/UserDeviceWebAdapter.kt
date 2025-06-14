package com.monglife.mongs.data.auth.web.adapter

import com.monglife.mongs.application.auth.port.web.UserDeviceWebPort
import com.monglife.mongs.application.auth.port.web.request.CreateDeviceRequest
import javax.inject.Inject

class UserDeviceWebAdapter @Inject constructor(

) : UserDeviceWebPort {

    override suspend fun createDevice(createDeviceRequest: CreateDeviceRequest) {
        TODO("Not yet implemented")
    }
}
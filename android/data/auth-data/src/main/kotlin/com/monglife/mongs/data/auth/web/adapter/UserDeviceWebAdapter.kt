package com.monglife.mongs.data.auth.web.adapter

import com.monglife.mongs.application.auth.exception.InvalidCreateUserDeviceException
import com.monglife.mongs.application.auth.port.web.UserDeviceWebPort
import com.monglife.mongs.application.auth.port.web.request.CreateDeviceRequest
import com.monglife.mongs.data.auth.web.client.UserDeviceWebClient
import com.monglife.mongs.data.auth.web.client.request.CreateDeviceRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDeviceWebAdapter @Inject constructor(
    private val userDeviceWebClient: UserDeviceWebClient,
) : UserDeviceWebPort {

    /**
     * 기기 등록
     */
    @Throws(InvalidCreateUserDeviceException::class)
    override suspend fun createDevice(createDeviceRequest: CreateDeviceRequest): Unit =
        userDeviceWebClient.createDevice(
            createDeviceRequestDto = CreateDeviceRequestDto(
                deviceId = createDeviceRequest.deviceId,
                deviceName = createDeviceRequest.deviceName,
                appPackageName = createDeviceRequest.appPackageName,
                fcmToken = createDeviceRequest.fcmToken,
            )
        ).let { response ->
            response.takeIf { it.isSuccessful }?.body() ?: throw InvalidCreateUserDeviceException()
        }
}